package com.cyx.paradegroundbackend.constant;

/** 用户常量 — Session key、初始薪资、密码加密盐值、角色定义 */
public interface UserConstant {

    String USER_LOGIN_STATE = "user_login_state";

    int INIT_SALARY = 10000;

    String PASSWORD_SALT = "parade-ground-salt";

    String ROLE_USER = "user";

    String ROLE_ADMIN = "admin";
}
