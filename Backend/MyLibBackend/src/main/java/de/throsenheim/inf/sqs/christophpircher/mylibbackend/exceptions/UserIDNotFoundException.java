package de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom authentication exception thrown when a user cannot be found by their user ID.
 * <p>
 * This exception is used in scenarios where authentication is performed using a UUID-based user ID
 * rather than a traditional username, such as when validating a JWT.
 * </p>
 *
 * <p>It extends {@link AuthenticationException}, allowing Spring Security to handle it
 * as part of the authentication flow.</p>
 *
 * @see org.springframework.security.core.AuthenticationException
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.JwtService
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.security.JwtAuthFilter
 */
public class UserIDNotFoundException extends AuthenticationException {
    /**
     * Constructs a new {@code UserIDNotFoundException} with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public UserIDNotFoundException(String message) {
        super(message);
    }
}
