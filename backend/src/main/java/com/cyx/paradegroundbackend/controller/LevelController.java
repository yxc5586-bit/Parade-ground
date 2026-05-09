package com.cyx.paradegroundbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyx.paradegroundbackend.common.BaseResponse;
import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.common.PageResponse;
import com.cyx.paradegroundbackend.common.ResultUtils;
import com.cyx.paradegroundbackend.constant.UserConstant;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.model.dto.common.PageRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelGenerateRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelQueryRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelUpdateRequest;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import com.cyx.paradegroundbackend.model.vo.LevelGenerateVO;
import com.cyx.paradegroundbackend.model.vo.LevelQuestionVO;
import com.cyx.paradegroundbackend.model.vo.LoginUserVO;
import com.cyx.paradegroundbackend.service.AnswerRecordService;
import com.cyx.paradegroundbackend.service.LevelInfoService;
import com.cyx.paradegroundbackend.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关卡接口 — 关卡生成（AI）、当前关卡查询、关卡详情、精选关卡分页、以及管理员CRUD。
 * 生成流程：检查是否有进行中的关卡 → 调用AI生成 → 存库 → 写入Session
 */
@RestController
@RequestMapping("/level")
@Tag(name = "关卡接口", description = "关卡生成、当前题目和题面详情相关接口")
public class LevelController {

    /** Session key：当前进行中的关卡ID */
    private static final String CURRENT_LEVEL_ID = "current_level_id";

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private LevelInfoService levelInfoService;

    @Resource
    private AnswerRecordService answerRecordService;

    @PostMapping("/generate")
    @Operation(summary = "生成下一关", description = "为当前登录用户生成下一关，若已有进行中的关卡则直接返回当前关卡")
    public BaseResponse<LevelGenerateVO> generateLevel(
            @RequestBody(required = false) LevelGenerateRequest levelGenerateRequest,
            HttpServletRequest request) {
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        String currentLevelId = getCurrentLevelId(request);
        if (StringUtils.hasText(currentLevelId) && !hasAnsweredLevel(loginUserVO.getId(), currentLevelId)) {
            LevelInfo currentLevel = levelInfoService.getLevelByLevelId(currentLevelId);
            if (currentLevel != null) {
                return ResultUtils.success(new LevelGenerateVO(currentLevelId, "READY", false));
            }
            clearCurrentLevel(request);
        }

        if (levelGenerateRequest == null) {
            levelGenerateRequest = new LevelGenerateRequest();
        }
        LevelInfo levelInfo = levelInfoService.generateLevel(
                loginUserVO.getId(),
                loginUserVO.getCurrentSalary(),
                levelGenerateRequest
        );
        request.getSession().setAttribute(CURRENT_LEVEL_ID, levelInfo.getLevelId());
        return ResultUtils.success(new LevelGenerateVO(levelInfo.getLevelId(), "READY", true));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取关卡详情", description = "根据关卡业务编号获取题面详情")
    public BaseResponse<LevelQuestionVO> getLevelDetail(
            @Parameter(description = "关卡业务编号 levelId") @RequestParam String levelId,
            HttpServletRequest request) {
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        validateLevelOwnership(loginUserVO.getId(), request, levelId);
        LevelInfo levelInfo = levelInfoService.getLevelByLevelId(levelId);
        if (levelInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关卡不存在");
        }
        return ResultUtils.success(LevelQuestionVO.fromEntity(levelInfo));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前关卡", description = "获取当前登录用户仍在进行中的关卡题面")
    public BaseResponse<LevelQuestionVO> getCurrentLevel(HttpServletRequest request) {
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        String currentLevelId = getCurrentLevelId(request);
        if (!StringUtils.hasText(currentLevelId)) {
            return ResultUtils.success(null);
        }
        if (hasAnsweredLevel(loginUserVO.getId(), currentLevelId)) {
            clearCurrentLevel(request);
            return ResultUtils.success(null);
        }
        LevelInfo levelInfo = levelInfoService.getLevelByLevelId(currentLevelId);
        if (levelInfo == null) {
            clearCurrentLevel(request);
            return ResultUtils.success(null);
        }
        return ResultUtils.success(LevelQuestionVO.fromEntity(levelInfo));
    }

    @GetMapping("/featured/page")
    @Operation(summary = "分页获取精选关卡", description = "分页获取优先级 >= 999 的精选关卡列表")
    public BaseResponse<PageResponse<LevelQuestionVO>> listFeaturedLevelByPage(PageRequest pageRequest,
                                                                               HttpServletRequest request) {
        userInfoService.getCurrentUser(request);
        IPage<LevelInfo> page = levelInfoService.listFeaturedLevelByPage(pageRequest);
        List<LevelQuestionVO> records = page.getRecords().stream()
                .map(LevelQuestionVO::fromEntity)
                .toList();
        return ResultUtils.success(PageResponse.of(page, records));
    }

    @GetMapping("/admin/list")
    @Operation(summary = "管理员分页查询关卡列表", description = "管理员按条件分页查询所有关卡，按优先级降序排列")
    public BaseResponse<PageResponse<LevelInfo>> adminListLevelByPage(LevelQueryRequest levelQueryRequest,
                                                                      HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(PageResponse.fromPage(levelInfoService.listLevelByPage(levelQueryRequest)));
    }

    @PutMapping("/admin/update")
    @Operation(summary = "管理员更新关卡", description = "管理员根据 id 更新关卡信息，支持设置优先级")
    public BaseResponse<Boolean> adminUpdateLevel(@RequestBody LevelUpdateRequest levelUpdateRequest,
                                                  HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(levelInfoService.updateLevel(levelUpdateRequest));
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "管理员删除关卡", description = "管理员根据主键 id 逻辑删除关卡")
    public BaseResponse<Boolean> adminDeleteLevel(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(levelInfoService.deleteLevel(id));
    }

    @PutMapping("/admin/priority")
    @Operation(summary = "管理员设置关卡优先级", description = "管理员设置关卡优先级：0=普通, 99=提升, 999=精选, 9999=置顶")
    public BaseResponse<Boolean> adminSetPriority(@RequestParam Long id, @RequestParam Integer priority,
                                                  HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(levelInfoService.updateLevelPriority(id, priority));
    }

    private void checkAdmin(HttpServletRequest request) {
        LoginUserVO loginUserVO = userInfoService.getCurrentUser(request);
        if (!UserConstant.ROLE_ADMIN.equals(loginUserVO.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
        }
    }

    private void validateLevelOwnership(Long userId, HttpServletRequest request, String levelId) {
        String currentLevelId = getCurrentLevelId(request);
        if (StringUtils.hasText(currentLevelId) && Objects.equals(currentLevelId, levelId)) {
            return;
        }
        if (!hasAnsweredLevel(userId, levelId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关卡不存在或无权访问");
        }
    }

    private boolean hasAnsweredLevel(Long userId, String levelId) {
        AnswerRecord answerRecord = answerRecordService.lambdaQuery()
                .eq(AnswerRecord::getUserId, userId)
                .eq(AnswerRecord::getLevelId, levelId)
                .last("limit 1")
                .one();
        return answerRecord != null;
    }

    private String getCurrentLevelId(HttpServletRequest request) {
        Object value = request.getSession().getAttribute(CURRENT_LEVEL_ID);
        return value instanceof String stringValue ? stringValue : null;
    }

    private void clearCurrentLevel(HttpServletRequest request) {
        request.getSession().removeAttribute(CURRENT_LEVEL_ID);
    }
}
