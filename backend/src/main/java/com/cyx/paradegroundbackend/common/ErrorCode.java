package com.cyx.paradegroundbackend.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NOT_FOUND_ERROR(40400, "数据不存在"),
    OPERATION_ERROR(50000, "操作失败"),
    SYSTEM_ERROR(50001, "系统内部异常"),
    NOT_IMPLEMENTED_ERROR(50002, "功能暂未实现");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
