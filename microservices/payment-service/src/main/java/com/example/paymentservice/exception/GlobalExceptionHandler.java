package com.example.paymentservice.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.paymentservice.dto.response.ApiResponse;
import com.stripe.exception.StripeException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler to customize error responses Ensures all errors are
 * returned in ApiResponse format
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle Stripe API errors
     */
    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ApiResponse<Object>> handleStripeException(StripeException ex) {
        log.error("Stripe error occurred: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Payment processing error: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle validation errors from @Valid annotations Returns errors in
     * ApiResponse format with detailed field validation info
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // Create user-friendly error message
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        fieldErrors.forEach((field, message) -> errorMessage.append(field).append(" - ").append(message).append("; "));

        ApiResponse<Object> response = ApiResponse.error(400, errorMessage.toString().trim());

        // Add field-level validation details in metadata for frontend consumption
        response.setMetadata(Map.of("validationErrors", fieldErrors));

        return ResponseEntity.status(400).body(response);
    }

    /**
     * Handles exceptions related to database integrity, such as unique
     * constraint violations. This is used to catch duplicate service titles.
     *
     * @param ex The exception thrown by the database layer.
     * @return An ApiResponse with a 409 Conflict status.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage()); // Use WARN level for this

        // Create a user-friendly message.
        String message = "A record with the same identifier already exists.";
        // Check for our custom message from the service layer.
        if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
            message = ex.getMessage();
        }

        ApiResponse<Object> response = ApiResponse.error(HttpStatus.CONFLICT.value(), message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle general exceptions and return them in ApiResponse format
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(500,
                ex.getMessage() != null ? ex.getMessage() : "Internal server error");

        return ResponseEntity.status(500).body(response);
    }

    /**
     * Handles exceptions when a requested resource (like a Recruiter or
     * Candidate) cannot be found in the database.
     *
     * @param ex The ResourceNotFoundException.
     * @return An ApiResponse with a 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage()); // Use WARN as this is a client error, not a server
                                                             // failure.
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles exceptions related to business rule violations, such as trying to
     * create a CVthèque for a recruiter who already has one.
     *
     * @param ex The IllegalStateException thrown by the service layer.
     * @return An ApiResponse with a 409 Conflict status.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

}
