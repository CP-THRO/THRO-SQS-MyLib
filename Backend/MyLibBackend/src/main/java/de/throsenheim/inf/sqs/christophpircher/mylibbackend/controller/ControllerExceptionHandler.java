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
 * Exception handler for all Rest Controllers.
 * This automatically generates the ResponseEntities for the Exceptions handled here instead of in the controllers.
 */
@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles the exceptions caused by violation of the jakarta constraint annotations.
     *
     * @param ex      the exception to handle
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return ResponseEntity with ApiError object
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
     * Handles the exceptions caused by Jackson not being able to deserialize fields due to format error (e.g. price is a string instead of an int).
     *
     * @param ex      the exception to handle
     * @param headers the headers to use for the response
     * @param status  the status code to use for the response
     * @param request the current request
     * @return ResponseEntity with ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NotNull HttpMessageNotReadableException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), ex.getMessage());
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    /**
     * Handles the exceptions caused by connection errors during customer service api calls.
     * This does not apply to OrderController, as the car dealer service is only called in the background worker.
     *
     * @param ex      the exception to handle
     * @param request the current request
     * @return ResponseEntity with ApiError object
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiError> handleIOException(IOException ex, WebRequest request) {
        log.error("Could not connect to external API: {}", ex.getMessage(), ex);
        String message = "Could not connect to external API: " + ex.getMessage();
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY, message, message);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    /**
     * Handles the exceptions caused by unexpected status codes of customer service api calls.
     * This does not apply to car dealer service api calls as the car dealer service api is only called in the background worker.
     *
     * @param ex      the exception to handle
     * @param request the current request
     * @return ResponseEntity with ApiError object
     */
    @ExceptionHandler(UnexpectedStatusException.class)
    public ResponseEntity<ApiError> handleUnexpectedStatusException(UnexpectedStatusException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY, ex.getMessage(), ex.getLocalizedMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    /**
     * Handles the UsernameExistsException caused by trying to create a user with a username that already exists
     * @param ex the exception to handle
     * @param request the current request
     * @return ResponseEntity with ApiError object
     */
    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ApiError> handleUsernameExistsException(UsernameExistsException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, ex.getLocalizedMessage(), ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}