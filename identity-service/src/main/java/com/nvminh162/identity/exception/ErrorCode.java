package com.nvminh162.identity.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    INVALID_KEY(1001, "Key enum is invalid"),
    USER_NOT_FOUND(1001, "User not found"),
    INVALID_USERNAME(1002, "Username is invalid"),
    INVALID_PASSWORD(1003, "Password is invalid"),
    USERNAME_ALREADY_EXISTS(1004, "Username already exists")
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
