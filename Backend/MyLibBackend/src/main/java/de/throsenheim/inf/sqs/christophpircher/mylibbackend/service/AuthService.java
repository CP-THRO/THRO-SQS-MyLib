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
 * Class for handling authentication requests
 */
@Service
@AllArgsConstructor
public class AuthService {

    private CustomUserDetailsService customUserDetailsService;

    private JwtService jwtService;

    private AuthenticationManager authenticationManager;

    /**
     * Add a new user to the database
     * @param username Username of the new user
     * @param password Password of the new user
     * @throws UsernameExistsException If the username already exists in the database
     */
    public void createNewUser(String username, String password) throws UsernameExistsException {
        customUserDetailsService.addUser(username, password);
    }

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
