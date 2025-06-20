package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

/**
 * Exception thrown when attempting to create a new user with a username that already exists in the system.
 * <p>
 * This is typically used in user registration flows to indicate that the requested username
 * is already taken and cannot be reused.
 * </p>
 *
 * <p>It is intended to be caught at the controller layer and translated into an appropriate HTTP 409 (Conflict) response.</p>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.ControllerExceptionHandler
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.AuthService
 */
public class UsernameExistsException extends Exception {
    /**
     * Constructs a new {@code UsernameExistsException} with the specified detail message.
     *
     * @param msg the detail message explaining the reason for the exception
     */
    public UsernameExistsException(String msg) {super(msg);}
}
