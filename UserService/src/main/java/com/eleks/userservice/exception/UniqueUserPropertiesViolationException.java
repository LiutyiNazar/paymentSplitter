package com.eleks.userservice.exception;

public class UniqueUserPropertiesViolationException extends RuntimeException {
    public UniqueUserPropertiesViolationException(String message) {
        super(message);
    }
}