package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling authentication-related operations.
 * <p>
 * This includes user registration and authentication, as well as issuing JWT tokens
 * upon successful login.
 * </p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *     <li>Create new users via {@link CustomUserDetailsService}</li>
 *     <li>Authenticate credentials using {@link AuthenticationManager}</li>
 *     <li>Generate JWT tokens via {@link JwtService}</li>
 * </ul>
 * */
 @Service
@AllArgsConstructor
public class AuthService {

    private CustomUserDetailsService customUserDetailsService;

    private JwtService jwtService;

    private AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the application.
     *
     * @param username the desired username for the new user
     * @param password the desired password for the new user
     * @throws UsernameExistsException if a user with the given username already exists
     */
    public void createNewUser(String username, String password) throws UsernameExistsException {
        customUserDetailsService.addUser(username, password);
    }

    /**
     * Authenticates a user using username and password, and generates a JWT token upon success.
     *
     * @param username the user's username
     * @param password the user's password
     * @return a JWT token if authentication is successful
     * @throws UsernameNotFoundException if the credentials are invalid
     */
    public String authenticate(String username, String password) throws UsernameNotFoundException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(((UserPrincipal)customUserDetailsService.loadUserByUsername(username)).getUser());
        }else{
            throw new UsernameNotFoundException("Invalid username or password");
        }
    }
}
