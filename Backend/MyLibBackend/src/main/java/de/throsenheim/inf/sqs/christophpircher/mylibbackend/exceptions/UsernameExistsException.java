package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

/**
 * Exception to be thrown on user creation if the username already exists.
 */
public class UsernameExistsException extends Exception {
    public UsernameExistsException(String msg) {super(msg);}
}
