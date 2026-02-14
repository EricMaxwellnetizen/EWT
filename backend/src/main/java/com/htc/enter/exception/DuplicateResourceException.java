package com.htc.enter.exception;

public class DuplicateResourceException extends ApplicationException {
    
    public DuplicateResourceException(String msg) {
        super(msg, "ERR_DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String msg, Object data) {
        super(msg, "ERR_DUPLICATE_RESOURCE", data);
    }
}
