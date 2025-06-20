package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

/**
 * Custom exception thrown when an external API returns an unexpected HTTP status code.
 * <p>
 * This exception is typically used in service or proxy layers (e.g., OpenLibrary API wrappers)
 * to indicate that the response did not meet expected success criteria (e.g., 2xx status).
 * </p>
 *
 * <p>It allows centralized error handling via controller-level exception handlers.</p>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.ControllerExceptionHandler
 * @see java.io.IOException
 * @see Exception
 *
 */
public class UnexpectedStatusException extends Exception {
    /**
     * Constructs a new {@code UnexpectedStatusException} with the specified detail message.
     *
     * @param msg the detail message explaining the unexpected status
     */
    public UnexpectedStatusException(String msg) {
        super(msg);
    }
}