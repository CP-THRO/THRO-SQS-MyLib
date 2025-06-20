package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.ApiError;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for all REST controllers.
 * <p>
 * This class centralizes exception handling logic and converts exceptions into structured {@link ApiError} responses.
 * It extends {@link ResponseEntityExceptionHandler} to override default Spring behaviors and to provide
 * application-specific handling of known exceptions such as validation failures, deserialization errors,
 * API connectivity issues, and domain-specific problems.
 * </p>
 *
 * <p>All exceptions handled here return appropriate HTTP status codes and JSON-formatted error details.</p>
 *
 * @see ApiError
 * @see ControllerAdvice
 * @see ExceptionHandler
 */
@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles validation errors thrown when incoming JSON fails to meet Jakarta Bean Validation constraints.
     * <p>
     * Constructs an {@link ApiError} response with status 400 (Bad Request) including field-level error messages.
     * </p>
     *
     * @param ex      the validation exception
     * @param headers HTTP headers
     * @param status  HTTP status (will be 400)
     * @param request the web request
     * @return structured {@link ApiError} response
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getObjectName() + "->" + error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    /**
     * Handles malformed JSON or deserialization errors caused by invalid data types or formats in the request body.
     * <p>
     * Returns a 400 (Bad Request) response with details about the parse failure.
     * </p>
     *
     * @param ex      the exception thrown by Jackson
     * @param headers HTTP headers
     * @param status  HTTP status code (400)
     * @param request the web request
     * @return structured {@link ApiError} response
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NotNull HttpMessageNotReadableException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), ex.getMessage());
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    /**
     * Handles {@link IOException}s, typically thrown when external APIs are unavailable or unresponsive.
     * <p>
     * Returns a 502 (Bad Gateway) response indicating upstream failure.
     * </p>
     *
     * @param ex      the I/O exception
     * @param request the current web request
     * @return structured {@link ApiError} response
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiError> handleIOException(IOException ex, WebRequest request) {
        log.error("Could not connect to external API: {}", ex.getMessage(), ex);
        String message = "Could not connect to external API: " + ex.getMessage();
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY, message, message);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    /**
     * Handles {@link UnexpectedStatusException}s thrown when an external API responds with an unexpected HTTP status code.
     * <p>
     * Returns a 502 (Bad Gateway) to reflect an upstream failure or inconsistency.
     * </p>
     *
     * @param ex      the exception
     * @param request the web request
     * @return structured {@link ApiError} response
     */
    @ExceptionHandler(UnexpectedStatusException.class)
    public ResponseEntity<ApiError> handleUnexpectedStatusException(UnexpectedStatusException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY, ex.getMessage(), ex.getLocalizedMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    /**
     * Handles {@link UsernameExistsException}s thrown when attempting to create a user with an existing username.
     * <p>
     * Returns a 409 (Conflict) with error details.
     * </p>
     *
     * @param ex      the exception
     * @param request the web request
     * @return structured {@link ApiError} response
     */
    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ApiError> handleUsernameExistsException(UsernameExistsException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, ex.getLocalizedMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}