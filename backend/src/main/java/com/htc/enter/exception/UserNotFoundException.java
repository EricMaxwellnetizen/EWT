package com.htc.enter.exception;

public class UserNotFoundException extends ApplicationException {
    
    public UserNotFoundException(String msg) {
        super(msg, "ERR_USER_NOT_FOUND");
    }

    public UserNotFoundException(String msg, String userId) {
        super(msg, "ERR_USER_NOT_FOUND", userId);
    }

    public UserNotFoundException(String msg, Throwable cause) {
        super(msg, "ERR_USER_NOT_FOUND", cause);
    }
}