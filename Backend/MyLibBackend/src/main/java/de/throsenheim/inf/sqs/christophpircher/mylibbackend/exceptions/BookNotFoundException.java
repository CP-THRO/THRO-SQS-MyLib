package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

/**
 * Exception thrown when a requested book cannot be found in the database
 * or in an external source such as the OpenLibrary API.
 * <p>
 * This exception typically results in an HTTP 404 (Not Found) response when handled by a controller.
 * </p>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.ControllerExceptionHandler
 */
public class BookNotFoundException extends Exception {
    /**
     * Constructs a new {@code BookNotFoundException} with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public BookNotFoundException(String message) {
        super(message);
    }
}
