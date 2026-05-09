package com.cyx.paradegroundbackend.util;

import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** JSON编解码工具 — 统一序列化/反序列化，基于Spring管理的ObjectMapper，失败时抛出BusinessException */
@Component
public class JsonCodec {

    @Resource
    private ObjectMapper objectMapper;

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to serialize JSON");
        }
    }

    public <T> T fromJson(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text, clazz);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to parse JSON");
        }
    }

    public <T> T fromJson(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to parse JSON");
        }
    }
}
