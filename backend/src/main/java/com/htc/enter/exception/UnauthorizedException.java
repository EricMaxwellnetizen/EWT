package com.htc.enter.exception;

public class UnauthorizedException extends ApplicationException {
    
    public UnauthorizedException(String msg) {
        super(msg, "ERR_UNAUTHORIZED");
    }
}
