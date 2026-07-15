package com.app.mono.common.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s 조회 실패 - %s 값 '%s' 에 해당하는 데이터가 없습니다.",
                resourceName, fieldName, fieldValue));
    }
}
