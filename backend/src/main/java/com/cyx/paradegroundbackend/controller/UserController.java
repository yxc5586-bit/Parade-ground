package com.cyx.paradegroundbackend.controller;

import com.cyx.paradegroundbackend.common.BaseResponse;
import com.cyx.paradegroundbackend.common.ResultUtils;
import com.cyx.paradegroundbackend.model.dto.user.UserLoginRequest;
import com.cyx.paradegroundbackend.model.dto.user.UserRegisterRequest;
import com.cyx.paradegroundbackend.model.vo.LoginUserVO;
import com.cyx.paradegroundbackend.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口 — 处理注册、登录（Session机制）、注销以及获取当前登录用户信息。
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口", description = "用户注册、登录、注销和当前用户信息相关接口")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建平台用户账号，默认初始月薪为 10000 元")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ResultUtils.success(userInfoService.userRegister(userRegisterRequest));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录后会将登录态保存到当前会话")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        return ResultUtils.success(userInfoService.userLogin(userLoginRequest, request));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "清理当前登录会话中的用户登录态")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userInfoService.userLogout(request));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户", description = "从当前会话中获取已登录用户的基础信息")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userInfoService.getCurrentUser(request));
    }
}
