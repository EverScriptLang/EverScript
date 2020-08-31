package com.linkbyte.everscript;

public class UserRuntimeError extends RuntimeError {
    ESInstance instance;
    Token token;

    UserRuntimeError(ESInstance instance, String message, Token token) {
        super(token, message);
        this.instance = instance;
        this.token = token;
    }
}
