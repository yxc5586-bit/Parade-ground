package com.cyx.paradegroundbackend.exception;

import com.cyx.paradegroundbackend.common.ErrorCode;
import lombok.Getter;

/** 业务异常 — 携带ErrorCode中的code，由GlobalExceptionHandler统一捕获并返回给前端 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
    }
}
