package com.htc.enter.exception;

public class ForbiddenException extends ApplicationException {
    
    public ForbiddenException(String msg) {
        super(msg, "ERR_FORBIDDEN");
    }
}
