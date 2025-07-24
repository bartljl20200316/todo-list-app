package com.sf.todo.exception;

public class PermissionException extends RuntimeException {
    private String errorCode;

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
