package com.template.api.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error payload returned by GlobalExceptionHandler.
 * Uses List<FieldViolation> so multiple validation errors on the same field are preserved.
 */
public record ApiErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldViolation> fieldErrors
) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(false, status, error, message, path, Instant.now(), List.of());
    }

    public static ApiErrorResponse of(int status, String error, String message,
                                      String path, List<FieldViolation> fieldErrors) {
        return new ApiErrorResponse(false, status, error, message, path, Instant.now(), fieldErrors);
    }
}
