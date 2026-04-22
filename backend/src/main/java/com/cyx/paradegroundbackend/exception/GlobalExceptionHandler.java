package com.cyx.paradegroundbackend.exception;

import com.cyx.paradegroundbackend.common.BaseResponse;
import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Object> businessExceptionHandler(BusinessException e) {
        return new BaseResponse<>(e.getCode(), null, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<Object> runtimeExceptionHandler(RuntimeException e) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
