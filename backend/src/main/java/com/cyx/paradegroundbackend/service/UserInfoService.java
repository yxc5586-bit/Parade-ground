package com.cyx.paradegroundbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyx.paradegroundbackend.model.dto.user.UserLoginRequest;
import com.cyx.paradegroundbackend.model.dto.user.UserRegisterRequest;
import com.cyx.paradegroundbackend.model.entity.UserInfo;
import com.cyx.paradegroundbackend.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

public interface UserInfoService extends IService<UserInfo> {

    Long userRegister(UserRegisterRequest userRegisterRequest);

    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    LoginUserVO getCurrentUser(HttpServletRequest request);
}
