package com.htc.enter.exception;

public class ResourceNotFoundException extends ApplicationException {
    
    public ResourceNotFoundException(String msg) {
        super(msg, "ERR_RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String msg, String resourceId) {
        super(msg, "ERR_RESOURCE_NOT_FOUND", resourceId);
    }

    public ResourceNotFoundException(String msg, Throwable cause) {
        super(msg, "ERR_RESOURCE_NOT_FOUND", cause);
    }
}