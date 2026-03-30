package com.roommatematch.exception;

public record ErrorResponse(
        int status,
        String message,
        String timestamp,
        String path
) {
}
