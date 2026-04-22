package com.cyx.paradegroundbackend.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.constant.UserConstant;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.mapper.UserInfoMapper;
import com.cyx.paradegroundbackend.model.dto.user.UserLoginRequest;
import com.cyx.paradegroundbackend.model.dto.user.UserRegisterRequest;
import com.cyx.paradegroundbackend.model.entity.UserInfo;
import com.cyx.paradegroundbackend.model.vo.LoginUserVO;
import com.cyx.paradegroundbackend.service.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (!StringUtils.hasText(userAccount) || !StringUtils.hasText(userPassword) || !StringUtils.hasText(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于 4 位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于 8 位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        UserInfo existedUser = this.baseMapper.selectByUserAccount(userAccount);
        if (existedUser != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已存在");
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserAccount(userAccount);
        userInfo.setUserPassword(encryptPassword(userPassword));
        userInfo.setUserName(StringUtils.hasText(userRegisterRequest.getUserName())
                ? userRegisterRequest.getUserName()
                : "coder_player");
        userInfo.setCurrentSalary(UserConstant.INIT_SALARY);
        boolean saved = this.save(userInfo);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败");
        }
        return userInfo.getId();
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (!StringUtils.hasText(userAccount) || !StringUtils.hasText(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }

        UserInfo userInfo = this.baseMapper.selectByUserAccount(userAccount);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (!encryptPassword(userPassword).equals(userInfo.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        HttpSession session = request.getSession();
        session.setAttribute(UserConstant.USER_LOGIN_STATE, userInfo.getId());
        return LoginUserVO.fromEntity(userInfo);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return true;
    }

    @Override
    public LoginUserVO getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object userIdObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (!(userIdObj instanceof Long userId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        UserInfo userInfo = this.getById(userId);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录状态已失效");
        }
        return LoginUserVO.fromEntity(userInfo);
    }

    private String encryptPassword(String userPassword) {
        return SecureUtil.md5(UserConstant.PASSWORD_SALT + userPassword);
    }
}
