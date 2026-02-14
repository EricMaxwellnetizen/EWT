package com.htc.enter.exception;

public abstract class ApplicationException extends RuntimeException {
    private String errorCode;
    private Object data;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = "UNKNOWN";
    }

    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, String errorCode, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getData() {
        return data;
    }
}
