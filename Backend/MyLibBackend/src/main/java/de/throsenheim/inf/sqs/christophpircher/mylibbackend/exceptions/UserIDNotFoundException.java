package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception, since I use the User ID instead of the Username for the JWT token
 */
public class UserIDNotFoundException extends AuthenticationException {
    public UserIDNotFoundException(String message) {
        super(message);
    }
}
