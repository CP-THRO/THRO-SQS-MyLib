package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Class for handling authentication requests
 */
@Service
@AllArgsConstructor
public class AuthService {

    private CustomUserDetailsService customUserDetailsService;

    /**
     * Add a new user to the database
     * @param username Username of the new user
     * @param password Password of the new user
     * @throws UsernameExistsException If the username already exists in the database
     */
    public void createNewUser(String username, String password) throws UsernameExistsException {
        customUserDetailsService.addUser(username, password);
    }


}
