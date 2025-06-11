package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

/**
 * Exception to be thrown if an api returns an unexpected status code.
 */
public class UnexpectedStatusException extends Exception {
    public UnexpectedStatusException(String msg) {
        super(msg);
    }
}