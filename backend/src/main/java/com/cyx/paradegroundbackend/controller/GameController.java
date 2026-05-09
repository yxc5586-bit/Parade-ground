package com.cyx.paradegroundbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyx.paradegroundbackend.common.BaseResponse;
import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.common.PageResponse;
import com.cyx.paradegroundbackend.common.ResultUtils;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.model.dto.game.GameRecordPageRequest;
import com.cyx.paradegroundbackend.model.dto.game.GameSubmitRequest;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import com.cyx.paradegroundbackend.model.vo.GameRecordDetailVO;
import com.cyx.paradegroundbackend.model.vo.GameRecordSummaryVO;
import com.cyx.paradegroundbackend.model.vo.GameSubmitResultVO;
import com.cyx.paradegroundbackend.model.vo.LoginUserVO;
import com.cyx.paradegroundbackend.service.AnswerRecordService;
import com.cyx.paradegroundbackend.service.LevelInfoService;
import com.cyx.paradegroundbackend.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 闯关接口 — 负责用户提交答案、查看战绩，答题流程通过 GameSubmitRequest → AI评审 → AnswerRecord 完成闭环。
 */
@RestController
@RequestMapping("/game")
@Tag(name = "闯关接口", description = "提交答案和历史战绩相关接口")
public class GameController {

    /** Session key：当前进行中的关卡ID */
    private static final String CURRENT_LEVEL_ID = "current_level_id";

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private LevelInfoService levelInfoService;

    @Resource
    private AnswerRecordService answerRecordService;

    @PostMapping("/submit")
    @Operation(summary = "提交答案", description = "提交当前关卡答案并返回判题结果")
    public BaseResponse<GameSubmitResultVO> submitAnswer(@RequestBody GameSubmitRequest gameSubmitRequest,
                                                         HttpServletRequest request) {
        if (gameSubmitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        AnswerRecord answerRecord = answerRecordService.submitAnswer(loginUserVO.getId(), gameSubmitRequest);
        request.getSession().removeAttribute(CURRENT_LEVEL_ID);
        return ResultUtils.success(GameSubmitResultVO.fromEntity(answerRecord));
    }

    @GetMapping("/records/page")
    @Operation(summary = "分页查看历史战绩", description = "分页查询当前登录用户的历史作答记录")
    public BaseResponse<PageResponse<GameRecordSummaryVO>> listMyRecords(GameRecordPageRequest gameRecordPageRequest,
                                                                         HttpServletRequest request) {
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        IPage<AnswerRecord> answerRecordPage = answerRecordService.listAnswerRecordByPage(
                loginUserVO.getId(),
                gameRecordPageRequest
        );
        Map<String, LevelInfo> levelInfoMap = getLevelInfoMap(answerRecordPage.getRecords());
        List<GameRecordSummaryVO> summaryVOList = answerRecordPage.getRecords().stream()
                .map(answerRecord -> {
                    LevelInfo levelInfo = levelInfoMap.get(answerRecord.getLevelId());
                    String levelName = levelInfo == null ? null : levelInfo.getLevelName();
                    return GameRecordSummaryVO.fromEntity(answerRecord, levelName);
                })
                .toList();
        return ResultUtils.success(PageResponse.of(answerRecordPage, summaryVOList));
    }

    @GetMapping("/records/detail")
    @Operation(summary = "查看战绩详情", description = "查看当前登录用户某条作答记录的详情")
    public BaseResponse<GameRecordDetailVO> getRecordDetail(
            @Parameter(description = "作答记录主键 recordId") @RequestParam Long recordId,
            HttpServletRequest request) {
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        AnswerRecord answerRecord = answerRecordService.getById(recordId);
        if (answerRecord == null || !Objects.equals(loginUserVO.getId(), answerRecord.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "作答记录不存在");
        }
        LevelInfo levelInfo = levelInfoService.getLevelByLevelId(answerRecord.getLevelId());
        return ResultUtils.success(GameRecordDetailVO.fromEntity(answerRecord, levelInfo));
    }

    private Map<String, LevelInfo> getLevelInfoMap(List<AnswerRecord> answerRecords) {
        if (answerRecords == null || answerRecords.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> levelIds = answerRecords.stream()
                .map(AnswerRecord::getLevelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (levelIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return levelInfoService.lambdaQuery()
                .in(LevelInfo::getLevelId, levelIds)
                .list()
                .stream()
                .collect(Collectors.toMap(LevelInfo::getLevelId, Function.identity(), (left, right) -> left));
    }
}
