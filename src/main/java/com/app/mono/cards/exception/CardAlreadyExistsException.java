package com.app.mono.cards.exception;

import com.app.mono.common.exception.BusinessException;

public class CardAlreadyExistsException extends BusinessException {

    public CardAlreadyExistsException(String message){
        super(message);
    }

}
