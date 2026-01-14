package com.nvminh162.identity.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    INVALID_KEY(1001, "Key enum is invalid"),
    USER_NOT_FOUND(1002, "User not found"),
    USERNAME_ALREADY_EXISTS(1003, "Username already exists"),
    INVALID_USERNAME(1004, "Username is invalid"),
    INVALID_PASSWORD(1005, "Password is invalid"),
    UNAUTHENTICATED(1006, "Unauthenticated"),
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
