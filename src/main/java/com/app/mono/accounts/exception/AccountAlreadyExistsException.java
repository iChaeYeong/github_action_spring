package com.app.mono.accounts.exception;

import com.app.mono.common.exception.BusinessException;

public class AccountAlreadyExistsException extends BusinessException {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
