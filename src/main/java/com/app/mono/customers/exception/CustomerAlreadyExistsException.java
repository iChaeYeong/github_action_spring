package com.app.mono.accounts.exception;

import com.app.mono.common.exception.BusinessException;

public class CustomerAlreadyExistsException extends BusinessException {
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
