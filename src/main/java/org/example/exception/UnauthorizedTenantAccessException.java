package org.example.exception;

public class UnauthorizedTenantAccessException extends RuntimeException {
    public UnauthorizedTenantAccessException(String message) {
        super(message);
    }
}