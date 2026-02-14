package com.htc.enter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("[{}] Resource not found: {}", traceId, ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI(), ex.getErrorCode(), traceId);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("[{}] User not found: {}", traceId, ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), req.getRequestURI(), ex.getErrorCode(), traceId);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("[{}] Bad request: {}", traceId, ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI(), ex.getErrorCode(), traceId);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("[{}] Duplicate resource: {}", traceId, ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), req.getRequestURI(), ex.getErrorCode(), traceId);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req.getRequestURI(), ex.getErrorCode(), traceId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(e -> 
            errors.append(e.getField()).append(": ").append(e.getDefaultMessage()).append("; "));
        log.warn("[{}] Validation failed: {}", traceId, errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors.toString(), req.getRequestURI(), "ERR_VALIDATION", traceId);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        StringBuilder errors = new StringBuilder();
        ex.getConstraintViolations().forEach(cv -> 
            errors.append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Constraint Violation", errors.toString(), req.getRequestURI(), "ERR_CONSTRAINT", traceId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String msg = String.format("Invalid parameter '%s'", ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Type Mismatch", msg, req.getRequestURI(), "ERR_TYPE_MISMATCH", traceId);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON", "Invalid JSON format", req.getRequestURI(), "ERR_MALFORMED_JSON", traceId);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid credentials", req.getRequestURI(), "ERR_BAD_CREDENTIALS", traceId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        return buildResponse(HttpStatus.FORBIDDEN, "Access Forbidden", "Insufficient permissions", req.getRequestURI(), "ERR_ACCESS_DENIED", traceId);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String msg = extractDataIntegrityMessage(ex.getMessage());
        log.error("[{}] Data integrity violation", traceId);
        return buildResponse(HttpStatus.CONFLICT, "Data Integrity Violation", msg, req.getRequestURI(), "ERR_DATA_INTEGRITY", traceId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[{}] Unhandled exception for {}", traceId, req.getRequestURI(), ex);
        String msg = ex.getMessage() != null ? ex.getMessage() : "Internal server error";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", msg, req.getRequestURI(), "ERR_INTERNAL", traceId);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, String path, String errorCode, String traceId) {
        ErrorResponse response = new ErrorResponse(status.value(), error, message, path, errorCode, traceId);
        return new ResponseEntity<>(response, status);
    }

    private String extractDataIntegrityMessage(String exceptionMessage) {
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("Unique") || exceptionMessage.contains("UNIQUE")) {
                return "A resource with this value already exists";
            } else if (exceptionMessage.contains("Foreign key") || exceptionMessage.contains("FOREIGN")) {
                return "Cannot perform operation due to foreign key constraints";
            } else if (exceptionMessage.contains("NOT NULL")) {
                return "A required field is missing";
            }
        }
        return "Database constraint violation";
    }
}
