package com.cyx.paradegroundbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyx.paradegroundbackend.ai.dto.AiGeneratedLevel;
import com.cyx.paradegroundbackend.ai.dto.AiGenerationConstraints;
import com.cyx.paradegroundbackend.ai.dto.AiHistoryStats;
import com.cyx.paradegroundbackend.ai.dto.AiLevelOption;
import com.cyx.paradegroundbackend.ai.dto.AiLevelRequirement;
import com.cyx.paradegroundbackend.ai.dto.LevelGenerationAiRequest;
import com.cyx.paradegroundbackend.ai.service.LevelGenerationAiService;
import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.mapper.AnswerRecordMapper;
import com.cyx.paradegroundbackend.mapper.LevelInfoMapper;
import com.cyx.paradegroundbackend.model.dto.level.LevelAddRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelGenerateRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelQueryRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelUpdateRequest;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import com.cyx.paradegroundbackend.service.LevelInfoService;
import com.cyx.paradegroundbackend.util.CompanyAliasSanitizer;
import com.cyx.paradegroundbackend.util.JsonCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LevelInfoServiceImpl extends ServiceImpl<LevelInfoMapper, LevelInfo> implements LevelInfoService {

    private static final Logger log = LoggerFactory.getLogger(LevelInfoServiceImpl.class);
    private static final int MIN_OPTION_COUNT = 12;
    private static final int MAX_OPTION_COUNT = 18;
    private static final DateTimeFormatter LEVEL_ID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Resource
    private LevelGenerationAiService levelGenerationAiService;

    @Resource
    private AnswerRecordMapper answerRecordMapper;

    @Resource
    private JsonCodec jsonCodec;

    @Resource
    private CompanyAliasSanitizer companyAliasSanitizer;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String openRouterApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:}")
    private String openRouterModelName;

    @Value("${langchain4j.open-ai.chat-model.timeout:}")
    private String openRouterTimeout;

    @Override
    public Long addLevel(LevelAddRequest levelAddRequest) {
        if (levelAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validateLevelFields(
                levelAddRequest.getLevelId(),
                levelAddRequest.getLevelName(),
                levelAddRequest.getDifficulty(),
                levelAddRequest.getSalaryRange(),
                levelAddRequest.getTags(),
                levelAddRequest.getRequirement(),
                levelAddRequest.getOptions(),
                levelAddRequest.getCorrectOptionIds()
        );

        if (this.baseMapper.selectByLevelId(levelAddRequest.getLevelId()) != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Level id already exists");
        }

        LevelInfo levelInfo = new LevelInfo();
        BeanUtils.copyProperties(levelAddRequest, levelInfo);
        boolean saved = this.save(levelInfo);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to add level");
        }
        return levelInfo.getId();
    }

    @Override
    public boolean updateLevel(LevelUpdateRequest levelUpdateRequest) {
        if (levelUpdateRequest == null || levelUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LevelInfo oldLevelInfo = this.getById(levelUpdateRequest.getId());
        if (oldLevelInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Level not found");
        }
        validateLevelFields(
                levelUpdateRequest.getLevelId(),
                levelUpdateRequest.getLevelName(),
                levelUpdateRequest.getDifficulty(),
                levelUpdateRequest.getSalaryRange(),
                levelUpdateRequest.getTags(),
                levelUpdateRequest.getRequirement(),
                levelUpdateRequest.getOptions(),
                levelUpdateRequest.getCorrectOptionIds()
        );

        LevelInfo existedLevel = this.baseMapper.selectByLevelId(levelUpdateRequest.getLevelId());
        if (existedLevel != null && !existedLevel.getId().equals(levelUpdateRequest.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Level id already exists");
        }

        LevelInfo levelInfo = new LevelInfo();
        BeanUtils.copyProperties(levelUpdateRequest, levelInfo);
        return this.updateById(levelInfo);
    }

    @Override
    public LevelInfo getLevelByLevelId(String levelId) {
        if (!StringUtils.hasText(levelId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Level id cannot be blank");
        }
        return this.baseMapper.selectByLevelId(levelId);
    }

    @Override
    public IPage<LevelInfo> listLevelByPage(LevelQueryRequest levelQueryRequest) {
        LevelQueryRequest queryRequest = levelQueryRequest == null ? new LevelQueryRequest() : levelQueryRequest;
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        if (pageSize <= 0 || pageSize > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid page size");
        }

        LambdaQueryWrapper<LevelInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(queryRequest.getLevelId()), LevelInfo::getLevelId, queryRequest.getLevelId());
        queryWrapper.like(StringUtils.hasText(queryRequest.getLevelName()), LevelInfo::getLevelName, queryRequest.getLevelName());
        queryWrapper.eq(StringUtils.hasText(queryRequest.getDifficulty()), LevelInfo::getDifficulty, queryRequest.getDifficulty());
        queryWrapper.eq(StringUtils.hasText(queryRequest.getSalaryRange()), LevelInfo::getSalaryRange, queryRequest.getSalaryRange());
        queryWrapper.orderByDesc(LevelInfo::getCreateTime);
        return this.page(new Page<>(current, pageSize), queryWrapper);
    }

    @Override
    public LevelInfo generateLevel(Long userId, Integer currentSalary, LevelGenerateRequest levelGenerateRequest) {
        if (userId == null || currentSalary == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Level generation params are incomplete");
        }
        ensureAiConfigured();

        LevelGenerationAiRequest aiRequest = buildAiRequest(userId, currentSalary, levelGenerateRequest);
        AiGeneratedLevel aiGeneratedLevel;
        try {
            log.info("Calling OpenRouter to generate level, userId={}, currentSalary={}, preferredDirection={}",
                    userId,
                    currentSalary,
                    levelGenerateRequest == null ? null : levelGenerateRequest.getPreferredDirection());
            aiGeneratedLevel = levelGenerationAiService.generateLevel(jsonCodec.toJson(aiRequest));
        } catch (Exception e) {
            log.error("OpenRouter level generation failed, userId={}, currentSalary={}", userId, currentSalary, e);
            throw new BusinessException(
                    ErrorCode.OPERATION_ERROR,
                    buildAiFailureMessage("AI level generation failed", e),
                    e
            );
        }

        AiGeneratedLevel sanitizedLevel = sanitizeGeneratedLevel(aiGeneratedLevel, currentSalary, levelGenerateRequest);
        LevelInfo levelInfo = toLevelInfo(sanitizedLevel);
        boolean saved = this.save(levelInfo);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to save generated level");
        }
        return levelInfo;
    }

    private void ensureAiConfigured() {
        if (!StringUtils.hasText(openRouterApiKey)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OPENROUTER_API_KEY is not configured");
        }
    }

    private LevelGenerationAiRequest buildAiRequest(Long userId, Integer currentSalary, LevelGenerateRequest levelGenerateRequest) {
        LevelGenerationAiRequest aiRequest = new LevelGenerationAiRequest();
        aiRequest.setTask("generate_level");
        aiRequest.setCurrentSalary(currentSalary);
        aiRequest.setPreferredDirection(levelGenerateRequest == null ? null : levelGenerateRequest.getPreferredDirection());
        aiRequest.setDifficultyRule(buildDifficultyRule());
        aiRequest.setHistoryStats(buildHistoryStats(userId));
        aiRequest.setConstraints(buildGenerationConstraints());
        return aiRequest;
    }

    private Map<String, String> buildDifficultyRule() {
        Map<String, String> difficultyRule = new LinkedHashMap<>();
        difficultyRule.put("10000-15000", "基础业务系统与常规后端实现");
        difficultyRule.put("15000-25000", "模块设计、缓存、异步、监控");
        difficultyRule.put("25000-35000", "复杂业务、高可用、一致性、扩展性");
        difficultyRule.put("35000-50000", "架构设计、灰度发布、容灾治理");
        difficultyRule.put("50000+", "平台化、长期演进、技术治理");
        return difficultyRule;
    }

    private AiGenerationConstraints buildGenerationConstraints() {
        AiGenerationConstraints constraints = new AiGenerationConstraints();
        constraints.setMinOptions(MIN_OPTION_COUNT);
        constraints.setMaxOptions(MAX_OPTION_COUNT);
        constraints.setUseRealCompanyNames(Boolean.FALSE);
        constraints.setBusinessSceneMustBeRealistic(Boolean.TRUE);
        return constraints;
    }

    private AiHistoryStats buildHistoryStats(Long userId) {
        List<AnswerRecord> answerRecords = answerRecordMapper.selectByUserId(userId);
        AiHistoryStats historyStats = new AiHistoryStats();
        historyStats.setTotalLevels(answerRecords.size());
        historyStats.setAvgScore(calculateAverageScore(answerRecords));
        historyStats.setStrongTags(new ArrayList<>());
        historyStats.setWeakTags(new ArrayList<>());
        if (answerRecords.isEmpty()) {
            return historyStats;
        }

        Set<String> levelIds = answerRecords.stream()
                .map(AnswerRecord::getLevelId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (levelIds.isEmpty()) {
            return historyStats;
        }

        Map<String, LevelInfo> levelInfoMap = this.lambdaQuery()
                .in(LevelInfo::getLevelId, levelIds)
                .list()
                .stream()
                .collect(Collectors.toMap(LevelInfo::getLevelId, levelInfo -> levelInfo, (left, right) -> left));

        Map<String, List<Integer>> tagScoreMap = new LinkedHashMap<>();
        for (AnswerRecord answerRecord : answerRecords) {
            LevelInfo levelInfo = levelInfoMap.get(answerRecord.getLevelId());
            if (levelInfo == null) {
                continue;
            }
            for (String tag : parseStringList(levelInfo.getTags())) {
                tagScoreMap.computeIfAbsent(tag, key -> new ArrayList<>()).add(answerRecord.getScore());
            }
        }

        List<Map.Entry<String, Double>> avgTagScores = tagScoreMap.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        entry.getValue().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0)
                ))
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .toList();
        historyStats.setStrongTags(avgTagScores.stream().limit(3).map(Map.Entry::getKey).toList());
        historyStats.setWeakTags(avgTagScores.stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList());
        return historyStats;
    }

    private int calculateAverageScore(List<AnswerRecord> answerRecords) {
        if (answerRecords == null || answerRecords.isEmpty()) {
            return 0;
        }
        return (int) Math.round(answerRecords.stream()
                .filter(record -> record.getScore() != null)
                .mapToInt(AnswerRecord::getScore)
                .average()
                .orElse(0));
    }

    private AiGeneratedLevel sanitizeGeneratedLevel(AiGeneratedLevel aiGeneratedLevel, Integer currentSalary,
                                                    LevelGenerateRequest levelGenerateRequest) {
        if (aiGeneratedLevel == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI returned empty level content");
        }
        if (!StringUtils.hasText(aiGeneratedLevel.getLevelName())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI level name is empty");
        }
        if (aiGeneratedLevel.getRequirement() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI level requirement is empty");
        }
        if (aiGeneratedLevel.getOptions() == null || aiGeneratedLevel.getOptions().size() < MIN_OPTION_COUNT
                || aiGeneratedLevel.getOptions().size() > MAX_OPTION_COUNT) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI options count is invalid");
        }
        if (aiGeneratedLevel.getCorrectOptionIds() == null || aiGeneratedLevel.getCorrectOptionIds().isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI correct answers are empty");
        }

        AiGeneratedLevel sanitizedLevel = new AiGeneratedLevel();
        sanitizedLevel.setLevelName(companyAliasSanitizer.sanitizeText(aiGeneratedLevel.getLevelName().trim()));
        sanitizedLevel.setDifficulty(buildDefaultDifficulty(currentSalary));
        sanitizedLevel.setSalaryRange(resolveSalaryRange(currentSalary));
        sanitizedLevel.setTags(sanitizeTags(aiGeneratedLevel.getTags(), levelGenerateRequest));
        sanitizedLevel.setRequirement(sanitizeRequirement(aiGeneratedLevel.getRequirement()));
        sanitizedLevel.setOptions(normalizeOptions(aiGeneratedLevel.getOptions()));
        sanitizedLevel.setCorrectOptionIds(normalizeCorrectOptionIds(
                aiGeneratedLevel.getOptions(),
                aiGeneratedLevel.getCorrectOptionIds(),
                sanitizedLevel.getOptions()
        ));
        sanitizedLevel.setAnalysisDirection(StringUtils.hasText(aiGeneratedLevel.getAnalysisDirection())
                ? companyAliasSanitizer.sanitizeText(aiGeneratedLevel.getAnalysisDirection().trim())
                : "本题主要考察业务需求分析、技术选型与系统设计的闭环能力。");

        if (sanitizedLevel.getCorrectOptionIds().size() < 5) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI correct answers are incomplete");
        }
        return sanitizedLevel;
    }

    private List<String> sanitizeTags(List<String> tags, LevelGenerateRequest levelGenerateRequest) {
        List<String> sanitizedTags = sanitizeStringList(tags);
        if (!sanitizedTags.isEmpty()) {
            return sanitizedTags;
        }
        List<String> fallbackTags = new ArrayList<>();
        if (levelGenerateRequest != null && StringUtils.hasText(levelGenerateRequest.getPreferredDirection())) {
            fallbackTags.add(companyAliasSanitizer.sanitizeText(levelGenerateRequest.getPreferredDirection().trim()));
        }
        fallbackTags.add("系统设计");
        return fallbackTags;
    }

    private AiLevelRequirement sanitizeRequirement(AiLevelRequirement requirement) {
        AiLevelRequirement sanitizedRequirement = new AiLevelRequirement();
        sanitizedRequirement.setBackground(requireText(requirement.getBackground(), "AI requirement background is empty"));
        sanitizedRequirement.setTarget(requireText(requirement.getTarget(), "AI requirement target is empty"));
        sanitizedRequirement.setRoles(sanitizeStringList(requirement.getRoles()));
        sanitizedRequirement.setCoreProcesses(sanitizeStringList(requirement.getCoreProcesses()));
        sanitizedRequirement.setRules(sanitizeStringList(requirement.getRules()));
        sanitizedRequirement.setPerformanceRequirements(sanitizeStringList(requirement.getPerformanceRequirements()));
        sanitizedRequirement.setStabilityRequirements(sanitizeStringList(requirement.getStabilityRequirements()));
        sanitizedRequirement.setSecurityRequirements(sanitizeStringList(requirement.getSecurityRequirements()));
        sanitizedRequirement.setExceptionHandling(sanitizeStringList(requirement.getExceptionHandling()));
        sanitizedRequirement.setLaunchRequirements(sanitizeStringList(requirement.getLaunchRequirements()));
        sanitizedRequirement.setNonFunctionalRequirements(sanitizeStringList(requirement.getNonFunctionalRequirements()));
        return sanitizedRequirement;
    }

    private String requireText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, message);
        }
        return companyAliasSanitizer.sanitizeText(text.trim());
    }

    private List<AiLevelOption> normalizeOptions(List<AiLevelOption> options) {
        List<AiLevelOption> normalizedOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            AiLevelOption option = options.get(i);
            if (option == null || !StringUtils.hasText(option.getContent())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI option content is empty");
            }
            AiLevelOption normalizedOption = new AiLevelOption();
            normalizedOption.setId(String.valueOf((char) ('A' + i)));
            normalizedOption.setContent(companyAliasSanitizer.sanitizeText(option.getContent().trim()));
            normalizedOption.setType("option");
            normalizedOptions.add(normalizedOption);
        }
        return normalizedOptions;
    }

    private List<String> normalizeCorrectOptionIds(List<AiLevelOption> originalOptions, List<String> originalCorrectIds,
                                                   List<AiLevelOption> normalizedOptions) {
        Map<String, String> optionIdMapping = new LinkedHashMap<>();
        for (int i = 0; i < originalOptions.size(); i++) {
            String normalizedId = normalizedOptions.get(i).getId();
            optionIdMapping.put(normalizedId, normalizedId);
            if (originalOptions.get(i) != null && StringUtils.hasText(originalOptions.get(i).getId())) {
                optionIdMapping.put(originalOptions.get(i).getId().trim().toUpperCase(), normalizedId);
            }
        }

        Set<String> normalizedCorrectIds = new LinkedHashSet<>();
        for (String originalCorrectId : originalCorrectIds) {
            if (!StringUtils.hasText(originalCorrectId)) {
                continue;
            }
            String mappedId = optionIdMapping.get(originalCorrectId.trim().toUpperCase());
            if (StringUtils.hasText(mappedId)) {
                normalizedCorrectIds.add(mappedId);
            }
        }
        if (normalizedCorrectIds.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI correct answers do not match options");
        }
        return new ArrayList<>(normalizedCorrectIds);
    }

    private List<String> sanitizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(companyAliasSanitizer::sanitizeText)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> parseStringList(String jsonText) {
        if (!StringUtils.hasText(jsonText)) {
            return Collections.emptyList();
        }
        try {
            return jsonCodec.fromJson(jsonText, new TypeReference<List<String>>() {
            });
        } catch (BusinessException e) {
            return Collections.emptyList();
        }
    }

    private LevelInfo toLevelInfo(AiGeneratedLevel generatedLevel) {
        LevelInfo levelInfo = new LevelInfo();
        levelInfo.setLevelId(generateUniqueLevelId());
        levelInfo.setLevelName(generatedLevel.getLevelName());
        levelInfo.setDifficulty(generatedLevel.getDifficulty());
        levelInfo.setSalaryRange(generatedLevel.getSalaryRange());
        levelInfo.setTags(jsonCodec.toJson(generatedLevel.getTags()));
        levelInfo.setRequirement(jsonCodec.toJson(generatedLevel.getRequirement()));
        levelInfo.setOptions(jsonCodec.toJson(generatedLevel.getOptions()));
        levelInfo.setCorrectOptionIds(jsonCodec.toJson(generatedLevel.getCorrectOptionIds()));
        levelInfo.setAnalysisDirection(generatedLevel.getAnalysisDirection());
        return levelInfo;
    }

    private String generateUniqueLevelId() {
        for (int i = 0; i < 10; i++) {
            String candidate = "level_" + LocalDateTime.now().format(LEVEL_ID_TIME_FORMATTER) + "_"
                    + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (this.baseMapper.selectByLevelId(candidate) == null) {
                return candidate;
            }
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to generate unique level id");
    }

    private String resolveSalaryRange(Integer currentSalary) {
        if (currentSalary == null || currentSalary <= 15000) {
            return "10000-15000";
        }
        if (currentSalary <= 25000) {
            return "15000-25000";
        }
        if (currentSalary <= 35000) {
            return "25000-35000";
        }
        if (currentSalary <= 50000) {
            return "35000-50000";
        }
        return "50000+";
    }

    private String buildDefaultDifficulty(Integer currentSalary) {
        if (currentSalary == null || currentSalary <= 15000) {
            return "basic";
        }
        if (currentSalary <= 25000) {
            return "intermediate";
        }
        if (currentSalary <= 35000) {
            return "advanced";
        }
        if (currentSalary <= 50000) {
            return "expert";
        }
        return "platform";
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
            return prefix + ": AI returned incomplete JSON. Increase OPENROUTER_MAX_TOKENS or reduce the generated level size.";
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

    private void validateLevelFields(String levelId, String levelName, String difficulty, String salaryRange,
                                     String tags, String requirement, String options, String correctOptionIds) {
        if (!StringUtils.hasText(levelId)
                || !StringUtils.hasText(levelName)
                || !StringUtils.hasText(difficulty)
                || !StringUtils.hasText(salaryRange)
                || !StringUtils.hasText(tags)
                || !StringUtils.hasText(requirement)
                || !StringUtils.hasText(options)
                || !StringUtils.hasText(correctOptionIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Level fields cannot be empty");
        }
    }
}
