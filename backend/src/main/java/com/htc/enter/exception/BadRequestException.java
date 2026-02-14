package com.htc.enter.exception;

public class BadRequestException extends ApplicationException {
    
    public BadRequestException(String msg) {
        super(msg, "ERR_BAD_REQUEST");
    }

    public BadRequestException(String msg, String errorCode) {
        super(msg, errorCode);
    }
}