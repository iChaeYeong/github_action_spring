package com.app.mono.loans.exception;

import com.app.mono.common.exception.BusinessException;

public class LoanAlreadyExistsException extends BusinessException {

    public LoanAlreadyExistsException(String message){
        super(message);
    }

}
