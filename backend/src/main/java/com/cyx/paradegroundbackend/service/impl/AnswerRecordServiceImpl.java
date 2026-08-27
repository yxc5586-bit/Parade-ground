package com.cyx.paradegroundbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyx.paradegroundbackend.ai.dto.AiJudgeLevel;
import com.cyx.paradegroundbackend.ai.dto.AiJudgeResult;
import com.cyx.paradegroundbackend.ai.dto.AiJudgeRules;
import com.cyx.paradegroundbackend.ai.dto.AiJudgeUser;
import com.cyx.paradegroundbackend.ai.dto.AiJudgeUserAnswer;
import com.cyx.paradegroundbackend.ai.dto.AiJobSuggestion;
import com.cyx.paradegroundbackend.ai.dto.AiLevelOption;
import com.cyx.paradegroundbackend.ai.dto.AiLevelRequirement;
import com.cyx.paradegroundbackend.ai.dto.AiReasonAnalysis;
import com.cyx.paradegroundbackend.ai.dto.AnswerJudgeAiRequest;
import com.cyx.paradegroundbackend.ai.service.AnswerJudgeAiService;
import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.constant.UserConstant;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.mapper.AnswerRecordMapper;
import com.cyx.paradegroundbackend.model.dto.answerrecord.AnswerRecordAddRequest;
import com.cyx.paradegroundbackend.model.dto.answerrecord.AnswerRecordUpdateRequest;
import com.cyx.paradegroundbackend.model.dto.game.GameRecordPageRequest;
import com.cyx.paradegroundbackend.model.dto.game.GameSubmitRequest;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import com.cyx.paradegroundbackend.model.entity.UserInfo;
import com.cyx.paradegroundbackend.service.AnswerRecordService;
import com.cyx.paradegroundbackend.service.AnswerSettlementService;
import com.cyx.paradegroundbackend.service.LevelInfoService;
import com.cyx.paradegroundbackend.service.UserInfoService;
import com.cyx.paradegroundbackend.util.CompanyAliasSanitizer;
import com.cyx.paradegroundbackend.util.JsonCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 作答记录服务实现 — 核心模块：
 * 1. 答案提交（submitAnswer）：组装AI评审请求 → 调用OpenRouter判题 → 计算得分 → 短事务结算
 * 2. 得分计算：基于precision/recall的F1-style评分算法，结合薪资档位计算薪资变化
 * 3. 容错兜底：AI返回异常时，使用本地规则计算得分并生成默认评价内容
 * 4. 公司名混淆：所有AI产出文本通过CompanyAliasSanitizer替换真实公司名
 */
@Service
public class AnswerRecordServiceImpl extends ServiceImpl<AnswerRecordMapper, AnswerRecord> implements AnswerRecordService {

    private static final Logger log = LoggerFactory.getLogger(AnswerRecordServiceImpl.class);

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private LevelInfoService levelInfoService;

    @Resource
    private AnswerJudgeAiService answerJudgeAiService;

    @Resource
    private JsonCodec jsonCodec;

    @Resource
    private CompanyAliasSanitizer companyAliasSanitizer;

    @Resource
    private AnswerRecordMapper answerRecordMapper;

    @Resource
    private AnswerSettlementService answerSettlementService;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String openRouterApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:}")
    private String openRouterModelName;

    @Value("${langchain4j.open-ai.chat-model.timeout:}")
    private String openRouterTimeout;

    @Override
    public Long addAnswerRecord(AnswerRecordAddRequest answerRecordAddRequest) {
        if (answerRecordAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validateAnswerRecordFields(
                answerRecordAddRequest.getUserId(),
                answerRecordAddRequest.getLevelId(),
                answerRecordAddRequest.getSelectedOptionIds(),
                answerRecordAddRequest.getCorrectOptionIds()
        );
        validateReferenceData(answerRecordAddRequest.getUserId(), answerRecordAddRequest.getLevelId());

        AnswerRecord answerRecord = new AnswerRecord();
        BeanUtils.copyProperties(answerRecordAddRequest, answerRecord);
        fillDefaultAnswerFields(answerRecord);
        boolean saved = this.save(answerRecord);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to add answer record");
        }
        return answerRecord.getId();
    }

    @Override
    public boolean updateAnswerRecord(AnswerRecordUpdateRequest answerRecordUpdateRequest) {
        if (answerRecordUpdateRequest == null || answerRecordUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AnswerRecord oldAnswerRecord = this.getById(answerRecordUpdateRequest.getId());
        if (oldAnswerRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Answer record not found");
        }
        validateAnswerRecordFields(
                answerRecordUpdateRequest.getUserId(),
                answerRecordUpdateRequest.getLevelId(),
                answerRecordUpdateRequest.getSelectedOptionIds(),
                answerRecordUpdateRequest.getCorrectOptionIds()
        );
        validateReferenceData(answerRecordUpdateRequest.getUserId(), answerRecordUpdateRequest.getLevelId());

        AnswerRecord answerRecord = new AnswerRecord();
        BeanUtils.copyProperties(answerRecordUpdateRequest, answerRecord);
        fillDefaultAnswerFields(answerRecord);
        return this.updateById(answerRecord);
    }

    @Override
    public IPage<AnswerRecord> listAnswerRecordByPage(Long userId, GameRecordPageRequest gameRecordPageRequest) {
        GameRecordPageRequest queryRequest = gameRecordPageRequest == null ? new GameRecordPageRequest() : gameRecordPageRequest;
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        if (pageSize <= 0 || pageSize > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid page size");
        }

        LambdaQueryWrapper<AnswerRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, AnswerRecord::getUserId, userId);
        queryWrapper.eq(StringUtils.hasText(queryRequest.getLevelId()), AnswerRecord::getLevelId, queryRequest.getLevelId());
        queryWrapper.orderByDesc(AnswerRecord::getCreateTime);
        return this.page(new Page<>(current, pageSize), queryWrapper);
    }

    @Override
    public AnswerRecord submitAnswer(Long userId, GameSubmitRequest gameSubmitRequest) {
        if (gameSubmitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId == null
                || !StringUtils.hasText(gameSubmitRequest.getLevelId())
                || CollectionUtils.isEmpty(gameSubmitRequest.getSelectedOptionIds())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Answer submit params are incomplete");
        }

        AnswerRecord existing = answerRecordMapper.selectByUserIdAndLevelId(userId, gameSubmitRequest.getLevelId());
        if (existing != null) {
            return existing;
        }

        ensureAiConfigured();
        UserInfo userInfo = loadUser(userId);
        LevelInfo levelInfo = loadLevel(gameSubmitRequest.getLevelId());

        AiLevelRequirement requirement = parseLevelRequirement(levelInfo.getRequirement());
        List<AiLevelOption> options = parseLevelOptions(levelInfo.getOptions());
        List<String> correctOptionIds = parseCorrectOptionIds(levelInfo.getCorrectOptionIds());
        List<String> validatedSelectedOptionIds = validateSelectedOptionIds(gameSubmitRequest.getSelectedOptionIds(), options);

        AnswerJudgeAiRequest aiRequest = buildJudgeRequest(
                userInfo,
                levelInfo,
                requirement,
                options,
                correctOptionIds,
                validatedSelectedOptionIds
        );
        AiJudgeResult aiJudgeResult;
        try {
            log.info("Calling OpenRouter to judge answer, userId={}, levelId={}", userId, gameSubmitRequest.getLevelId());
            aiJudgeResult = answerJudgeAiService.judgeAnswer(jsonCodec.toJson(aiRequest));
        } catch (Exception e) {
            log.error("OpenRouter answer judging failed, userId={}, levelId={}", userId, gameSubmitRequest.getLevelId(), e);
            throw new BusinessException(
                    ErrorCode.OPERATION_ERROR,
                    buildAiFailureMessage("AI answer judging failed", e),
                    e
            );
        }

        AiJudgeResult sanitizedResult = sanitizeJudgeResult(
                aiJudgeResult,
                userInfo.getCurrentSalary(),
                options,
                correctOptionIds,
                validatedSelectedOptionIds,
                requirement
        );

        AnswerRecord answerRecord = buildAnswerRecord(
                userInfo.getId(),
                validatedSelectedOptionIds,
                gameSubmitRequest.getClientSpendSeconds(),
                levelInfo.getLevelId(),
                correctOptionIds,
                sanitizedResult
        );
        int expectedSalary = defaultIfNull(userInfo.getCurrentSalary(), UserConstant.INIT_SALARY);
        return answerSettlementService.settle(answerRecord, expectedSalary, sanitizedResult.getUpdatedSalary());
    }

    private void ensureAiConfigured() {
        if (!StringUtils.hasText(openRouterApiKey)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OPENROUTER_API_KEY is not configured");
        }
    }

    private UserInfo loadUser(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "User not found");
        }
        return userInfo;
    }

    private LevelInfo loadLevel(String levelId) {
        LevelInfo levelInfo = levelInfoService.getLevelByLevelId(levelId);
        if (levelInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Level not found");
        }
        return levelInfo;
    }

    private AiLevelRequirement parseLevelRequirement(String jsonText) {
        return jsonCodec.fromJson(jsonText, AiLevelRequirement.class);
    }

    private List<AiLevelOption> parseLevelOptions(String jsonText) {
        return jsonCodec.fromJson(jsonText, new TypeReference<List<AiLevelOption>>() {
        });
    }

    private List<String> parseCorrectOptionIds(String jsonText) {
        return jsonCodec.fromJson(jsonText, new TypeReference<List<String>>() {
        });
    }

    private AnswerJudgeAiRequest buildJudgeRequest(UserInfo userInfo, LevelInfo levelInfo, AiLevelRequirement requirement,
                                                   List<AiLevelOption> options, List<String> correctOptionIds,
                                                   List<String> selectedOptionIds) {
        AiJudgeUser judgeUser = new AiJudgeUser();
        judgeUser.setUserId(userInfo.getId());
        judgeUser.setCurrentSalary(userInfo.getCurrentSalary());

        AiJudgeLevel judgeLevel = new AiJudgeLevel();
        judgeLevel.setLevelId(levelInfo.getLevelId());
        judgeLevel.setLevelName(levelInfo.getLevelName());
        judgeLevel.setDifficulty(levelInfo.getDifficulty());
        judgeLevel.setRequirement(requirement);
        judgeLevel.setOptions(options);
        judgeLevel.setCorrectOptionIds(correctOptionIds);

        AiJudgeUserAnswer userAnswer = new AiJudgeUserAnswer();
        userAnswer.setSelectedOptionIds(selectedOptionIds);

        AiJudgeRules judgeRules = new AiJudgeRules();
        judgeRules.setFullScore(100);
        judgeRules.setOutputHumorStyle(Boolean.TRUE);
        judgeRules.setUseFakeCompanyNames(Boolean.TRUE);

        AnswerJudgeAiRequest aiRequest = new AnswerJudgeAiRequest();
        aiRequest.setTask("judge_level_answer");
        aiRequest.setUser(judgeUser);
        aiRequest.setLevel(judgeLevel);
        aiRequest.setUserAnswer(userAnswer);
        aiRequest.setRules(judgeRules);
        return aiRequest;
    }

    private AiJudgeResult sanitizeJudgeResult(AiJudgeResult aiJudgeResult, Integer currentSalary, List<AiLevelOption> options,
                                              List<String> correctOptionIds, List<String> selectedOptionIds,
                                              AiLevelRequirement requirement) {
        List<String> normalizedSelectedIds = normalizeOptionIds(selectedOptionIds);
        int baseSalary = defaultIfNull(currentSalary, UserConstant.INIT_SALARY);
        int score = calculateFallbackScore(normalizedSelectedIds, correctOptionIds);
        int salaryChange = calculateSalaryChange(score, baseSalary);
        int updatedSalary = Math.max(1000, baseSalary + salaryChange);

        AiJudgeResult sanitizedResult = new AiJudgeResult();
        sanitizedResult.setScore(score);
        sanitizedResult.setEvaluationTitle(sanitizeOrDefaultTitle(aiJudgeResult == null ? null : aiJudgeResult.getEvaluationTitle(), score));
        sanitizedResult.setEvaluationText(sanitizeOrDefaultEvaluation(aiJudgeResult == null ? null : aiJudgeResult.getEvaluationText(), score));
        sanitizedResult.setSalaryChange(salaryChange);
        sanitizedResult.setUpdatedSalary(updatedSalary);
        sanitizedResult.setJobSuggestions(sanitizeJobSuggestions(aiJudgeResult == null ? null : aiJudgeResult.getJobSuggestions(), updatedSalary));
        sanitizedResult.setReasonAnalysis(sanitizeReasonAnalysis(
                aiJudgeResult == null ? null : aiJudgeResult.getReasonAnalysis(),
                options,
                correctOptionIds,
                normalizedSelectedIds
        ));
        sanitizedResult.setStandardAnswers(new ArrayList<>(correctOptionIds));
        sanitizedResult.setDetailedSolution(buildDetailedSolution(
                aiJudgeResult == null ? null : aiJudgeResult.getDetailedSolution(),
                options,
                correctOptionIds,
                requirement
        ));
        return sanitizedResult;
    }

    private int calculateFallbackScore(List<String> selectedOptionIds, List<String> correctOptionIds) {
        Set<String> selectedSet = new LinkedHashSet<>(selectedOptionIds);
        Set<String> correctSet = new LinkedHashSet<>(correctOptionIds);
        if (selectedSet.isEmpty() || correctSet.isEmpty()) {
            return 0;
        }
        long hitCount = selectedSet.stream().filter(correctSet::contains).count();
        double precision = (double) hitCount / selectedSet.size();
        double recall = (double) hitCount / correctSet.size();
        return clampScore((int) Math.round(100 * precision * recall));
    }

    private int clampScore(Integer score) {
        if (score == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, score));
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private int calculateSalaryChange(int score, int currentSalary) {
        int normalizedScore = clampScore(score);
        int baseChange;
        if (normalizedScore >= 90) {
            baseChange = interpolate(normalizedScore, 90, 100, 500, 2000);
        } else if (normalizedScore >= 80) {
            baseChange = interpolate(normalizedScore, 80, 89, 200, 800);
        } else if (normalizedScore >= 70) {
            baseChange = interpolate(normalizedScore, 70, 79, 0, 300);
        } else if (normalizedScore >= 60) {
            baseChange = interpolate(normalizedScore, 60, 69, -500, -200);
        } else if (normalizedScore >= 50) {
            baseChange = interpolate(normalizedScore, 50, 59, -1000, -500);
        } else {
            baseChange = interpolate(normalizedScore, 0, 49, -2000, -1000);
        }
        return (int) Math.round(baseChange * resolveSalaryFactor(currentSalary));
    }

    private int interpolate(int score, int minScore, int maxScore, int minChange, int maxChange) {
        if (maxScore == minScore) {
            return minChange;
        }
        double ratio = (double) (score - minScore) / (maxScore - minScore);
        return (int) Math.round(minChange + (maxChange - minChange) * ratio);
    }

    private double resolveSalaryFactor(int currentSalary) {
        if (currentSalary < 10000) {
            return 0.8;
        }
        if (currentSalary < 20000) {
            return 1.0;
        }
        if (currentSalary <= 30000) {
            return 1.2;
        }
        return 1.5;
    }

    private String sanitizeOrDefaultTitle(String title, int score) {
        if (StringUtils.hasText(title)) {
            return companyAliasSanitizer.sanitizeText(title.trim());
        }
        if (score >= 90) {
            return "这一波像是把评委的工牌都刷亮了";
        }
        if (score >= 75) {
            return "工位稳了，产品经理决定先不加夜间需求";
        }
        if (score >= 60) {
            return "能交差，但上线前最好再补一轮自查";
        }
        if (score >= 40) {
            return "方案有点悬，评审会已经开始有人皱眉了";
        }
        return "这一题答完，工位边上已经响起危险预警";
    }

    private String sanitizeOrDefaultEvaluation(String evaluationText, int score) {
        if (StringUtils.hasText(evaluationText)) {
            return companyAliasSanitizer.sanitizeText(evaluationText.trim());
        }
        if (score >= 80) {
            return "你抓住了核心业务闭环和关键技术点，方案完整度不错，属于评审会上能让人点头的水准。";
        }
        if (score >= 60) {
            return "关键点基本摸到了，但可靠性、监控或边界处理还不够扎实，属于能做出来但离企业级还有一点距离。";
        }
        if (score >= 40) {
            return "你有一定方向感，但选型和闭环明显松动，评审会里大概率会被追着问细节。";
        }
        return "这份方案的问题已经不是一个小补丁能解决了，建议先把业务闭环和关键链路重新梳理一遍。";
    }

    private List<AiJobSuggestion> sanitizeJobSuggestions(List<AiJobSuggestion> jobSuggestions, int updatedSalary) {
        List<AiJobSuggestion> sanitized = new ArrayList<>();
        if (jobSuggestions != null) {
            for (AiJobSuggestion suggestion : jobSuggestions) {
                if (suggestion == null || !StringUtils.hasText(suggestion.getCompanyAlias())) {
                    continue;
                }
                AiJobSuggestion item = new AiJobSuggestion();
                item.setCompanyAlias(companyAliasSanitizer.sanitizeText(suggestion.getCompanyAlias().trim()));
                item.setFitReason(companyAliasSanitizer.sanitizeText(defaultText(
                        suggestion.getFitReason(),
                        "适合继续补齐业务系统设计与方案分析能力。"
                )));
                item.setSalaryRange(companyAliasSanitizer.sanitizeText(defaultText(
                        suggestion.getSalaryRange(),
                        resolveJobSalaryRange(updatedSalary)
                )));
                sanitized.add(item);
            }
        }
        if (!sanitized.isEmpty()) {
            return sanitized.stream().limit(3).toList();
        }
        return buildFallbackJobSuggestions(updatedSalary);
    }

    private List<AiJobSuggestion> buildFallbackJobSuggestions(int updatedSalary) {
        String salaryRange = resolveJobSalaryRange(updatedSalary);
        List<AiJobSuggestion> suggestions = new ArrayList<>();
        suggestions.add(buildJobSuggestion("阿巴阿巴零售云", "适合继续锻炼交易、库存、订单等中后台能力。", salaryRange));
        suggestions.add(buildJobSuggestion("企鹅大王生活服务", "适合积累异步处理、监控告警和业务闭环经验。", salaryRange));
        suggestions.add(buildJobSuggestion("字符跳跃商业平台", "适合继续补系统设计、性能优化和稳定性意识。", salaryRange));
        return suggestions;
    }

    private AiJobSuggestion buildJobSuggestion(String companyAlias, String fitReason, String salaryRange) {
        AiJobSuggestion suggestion = new AiJobSuggestion();
        suggestion.setCompanyAlias(companyAlias);
        suggestion.setFitReason(fitReason);
        suggestion.setSalaryRange(salaryRange);
        return suggestion;
    }

    private String resolveJobSalaryRange(int updatedSalary) {
        if (updatedSalary <= 15000) {
            return "10k-15k";
        }
        if (updatedSalary <= 25000) {
            return "15k-25k";
        }
        if (updatedSalary <= 35000) {
            return "25k-35k";
        }
        if (updatedSalary <= 50000) {
            return "35k-50k";
        }
        return "50k+";
    }

    private AiReasonAnalysis sanitizeReasonAnalysis(AiReasonAnalysis aiReasonAnalysis, List<AiLevelOption> options,
                                                    List<String> correctOptionIds, List<String> selectedOptionIds) {
        Map<String, String> optionMap = options.stream()
                .collect(Collectors.toMap(AiLevelOption::getId, AiLevelOption::getContent, (left, right) -> left, LinkedHashMap::new));
        List<String> selectedCorrect = selectedOptionIds.stream()
                .filter(correctOptionIds::contains)
                .map(optionId -> optionId + " " + optionMap.getOrDefault(optionId, "关键点"))
                .toList();
        List<String> selectedWrong = selectedOptionIds.stream()
                .filter(optionId -> !correctOptionIds.contains(optionId))
                .map(optionId -> optionId + " " + optionMap.getOrDefault(optionId, "不合适选项"))
                .toList();
        List<String> missed = correctOptionIds.stream()
                .filter(optionId -> !selectedOptionIds.contains(optionId))
                .map(optionId -> optionId + " " + optionMap.getOrDefault(optionId, "遗漏关键点"))
                .toList();

        AiReasonAnalysis sanitized = new AiReasonAnalysis();
        sanitized.setCorrectChoices(aiReasonAnalysis != null && !CollectionUtils.isEmpty(aiReasonAnalysis.getCorrectChoices())
                ? sanitizeTextList(aiReasonAnalysis.getCorrectChoices())
                : selectedCorrect.stream().map(text -> "命中了关键点：" + text).toList());
        sanitized.setWrongChoices(aiReasonAnalysis != null && !CollectionUtils.isEmpty(aiReasonAnalysis.getWrongChoices())
                ? sanitizeTextList(aiReasonAnalysis.getWrongChoices())
                : selectedWrong.stream().map(text -> "误选了不适合当前场景的方案：" + text).toList());
        sanitized.setMissedChoices(aiReasonAnalysis != null && !CollectionUtils.isEmpty(aiReasonAnalysis.getMissedChoices())
                ? sanitizeTextList(aiReasonAnalysis.getMissedChoices())
                : missed.stream().map(text -> "漏掉了关键闭环或治理点：" + text).toList());
        return sanitized;
    }

    private String buildDetailedSolution(String detailedSolution, List<AiLevelOption> options, List<String> correctOptionIds,
                                         AiLevelRequirement requirement) {
        if (StringUtils.hasText(detailedSolution)) {
            return companyAliasSanitizer.sanitizeText(detailedSolution.trim());
        }
        Map<String, String> optionMap = options.stream()
                .collect(Collectors.toMap(AiLevelOption::getId, AiLevelOption::getContent, (left, right) -> left, LinkedHashMap::new));
        String correctOptionSummary = correctOptionIds.stream()
                .map(optionId -> optionId + " 对应的是“" + optionMap.getOrDefault(optionId, "关键方案") + "”")
                .collect(Collectors.joining("，"));
        String target = StringUtils.hasText(requirement.getTarget()) ? requirement.getTarget() : "完成当前业务需求";
        return companyAliasSanitizer.sanitizeText(
                "企业里通常会先从业务目标出发，把“" + target + "”拆成核心链路、状态边界、异常兜底和上线保障四层。"
                        + "先明确主流程与角色职责，再确定数据流、状态流和关键依赖，避免只堆技术名词不补业务闭环。"
                        + "在实现层面，需要优先保证正确性、幂等性、监控告警和异常恢复，其次再考虑性能优化与扩展性。"
                        + "本题的标准答案里，" + correctOptionSummary
                        + "。这些点共同组成了一个可上线、可观测、可恢复的企业级方案。"
        );
    }

    private List<String> sanitizeTextList(List<String> textList) {
        if (textList == null) {
            return Collections.emptyList();
        }
        return textList.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(companyAliasSanitizer::sanitizeText)
                .toList();
    }

    private List<String> normalizeOptionIds(List<String> optionIds) {
        if (optionIds == null) {
            return Collections.emptyList();
        }
        return optionIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();
    }

    private String buildAiFailureMessage(String prefix, Exception exception) {
        String rootCauseMessage = extractRootCauseMessage(exception);
        String causeMessages = collectCauseMessages(exception);
        if (causeMessages.contains("Error while extracting response for type [java.lang.String]")
                && causeMessages.contains("closed")) {
            return prefix + ": OpenRouter closed the response while the backend was reading it. "
                    + "This usually means the model response took too long or was too large for the current synchronous request. "
                    + "Current model=" + readableConfigValue(openRouterModelName, "unknown")
                    + ", timeout=" + readableConfigValue(openRouterTimeout, "unknown")
                    + ". Use a faster OPENROUTER_MODEL_NAME or increase OPENROUTER_TIMEOUT.";
        }
        if (causeMessages.contains("Unexpected end-of-input")) {
            return prefix + ": AI returned incomplete JSON. Increase OPENROUTER_MAX_TOKENS or reduce the generated response size.";
        }
        if (causeMessages.contains("Error while extracting response for type [java.lang.String]")
                && causeMessages.contains("content type [application/json]")) {
            return prefix + ": OpenRouter returned an error response. Please check whether OPENROUTER_API_KEY is valid, "
                    + "the account has available credits, the model "
                    + readableConfigValue(openRouterModelName, "unknown")
                    + " is accessible, or the request was rate-limited.";
        }
        return prefix + ": " + rootCauseMessage;
    }

    private String collectCauseMessages(Throwable throwable) {
        StringBuilder messages = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (StringUtils.hasText(current.getMessage())) {
                if (!messages.isEmpty()) {
                    messages.append(" | ");
                }
                messages.append(current.getMessage().trim());
            }
            current = current.getCause();
        }
        return messages.toString();
    }

    private String extractRootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        String message = null;
        while (current != null) {
            if (StringUtils.hasText(current.getMessage())) {
                message = current.getMessage().trim();
            }
            current = current.getCause();
        }
        return StringUtils.hasText(message) ? message : "Unknown AI service error";
    }

    private String readableConfigValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private List<String> validateSelectedOptionIds(List<String> selectedOptionIds, List<AiLevelOption> options) {
        List<String> normalizedSelectedIds = normalizeOptionIds(selectedOptionIds);
        Set<String> availableOptionIds = options.stream()
                .map(AiLevelOption::getId)
                .filter(StringUtils::hasText)
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!availableOptionIds.containsAll(normalizedSelectedIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Selected options contain invalid ids");
        }
        return normalizedSelectedIds;
    }

    private AnswerRecord buildAnswerRecord(Long userId, List<String> selectedOptionIds, Integer clientSpendSeconds, String levelId,
                                           List<String> correctOptionIds, AiJudgeResult sanitizedResult) {
        AnswerRecord answerRecord = new AnswerRecord();
        answerRecord.setUserId(userId);
        answerRecord.setLevelId(levelId);
        answerRecord.setSelectedOptionIds(jsonCodec.toJson(selectedOptionIds));
        answerRecord.setCorrectOptionIds(jsonCodec.toJson(correctOptionIds));
        answerRecord.setClientSpendSeconds(clientSpendSeconds == null ? 0 : Math.max(0, clientSpendSeconds));
        answerRecord.setScore(sanitizedResult.getScore());
        answerRecord.setSalaryChange(sanitizedResult.getSalaryChange());
        answerRecord.setUpdatedSalary(sanitizedResult.getUpdatedSalary());
        answerRecord.setResultReport(jsonCodec.toJson(buildResultReportPayload(sanitizedResult)));
        fillDefaultAnswerFields(answerRecord);
        return answerRecord;
    }

    private Map<String, Object> buildResultReportPayload(AiJudgeResult sanitizedResult) {
        Map<String, Object> resultReport = new LinkedHashMap<>();
        resultReport.put("evaluationTitle", sanitizedResult.getEvaluationTitle());
        resultReport.put("evaluationText", sanitizedResult.getEvaluationText());
        resultReport.put("jobSuggestions", sanitizedResult.getJobSuggestions());
        resultReport.put("reasonAnalysis", sanitizedResult.getReasonAnalysis());
        resultReport.put("detailedSolution", sanitizedResult.getDetailedSolution());
        return resultReport;
    }

    private String defaultText(String text, String defaultValue) {
        return StringUtils.hasText(text) ? text.trim() : defaultValue;
    }

    private void validateAnswerRecordFields(Long userId, String levelId, String selectedOptionIds, String correctOptionIds) {
        if (userId == null
                || !StringUtils.hasText(levelId)
                || !StringUtils.hasText(selectedOptionIds)
                || !StringUtils.hasText(correctOptionIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Answer record fields cannot be empty");
        }
    }

    private void validateReferenceData(Long userId, String levelId) {
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "User not found");
        }
        LevelInfo levelInfo = levelInfoService.getLevelByLevelId(levelId);
        if (levelInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Level not found");
        }
    }

    private void fillDefaultAnswerFields(AnswerRecord answerRecord) {
        if (answerRecord.getClientSpendSeconds() == null) {
            answerRecord.setClientSpendSeconds(0);
        }
        if (answerRecord.getScore() == null) {
            answerRecord.setScore(0);
        }
        if (answerRecord.getSalaryChange() == null) {
            answerRecord.setSalaryChange(0);
        }
        if (answerRecord.getUpdatedSalary() == null) {
            answerRecord.setUpdatedSalary(UserConstant.INIT_SALARY);
        }
        if (answerRecord.getResultReport() == null) {
            answerRecord.setResultReport("");
        }
    }
}
