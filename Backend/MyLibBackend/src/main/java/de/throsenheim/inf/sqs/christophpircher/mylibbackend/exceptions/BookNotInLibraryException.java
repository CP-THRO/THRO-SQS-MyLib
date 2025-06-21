package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

/**
 * Exception thrown when a user attempts to perform an action on a book
 * that is not part of their personal library.
 * <p>
 * This typically applies to operations like rating or updating the reading status of a book
 * that the user has not yet added to their library.
 * </p>
 *
 * <p>This exception is generally translated to an HTTP 404 (Not Found) response.</p>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.ControllerExceptionHandler
 */
public class BookNotInLibraryException extends Exception {
    /**
     * Constructs a new {@code BookNotInLibraryException} with the specified detail message.
     *
     * @param message the detail message explaining the reason the exception was thrown
     */
    public BookNotInLibraryException(String message) {super(message);}
}
