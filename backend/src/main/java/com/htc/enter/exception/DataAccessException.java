package com.htc.enter.exception;

public class DataAccessException extends ApplicationException {
    
    public DataAccessException(String msg) {
        super(msg, "ERR_DATA_ACCESS");
    }

    public DataAccessException(String msg, Throwable cause) {
        super(msg, "ERR_DATA_ACCESS", cause);
    }
}
