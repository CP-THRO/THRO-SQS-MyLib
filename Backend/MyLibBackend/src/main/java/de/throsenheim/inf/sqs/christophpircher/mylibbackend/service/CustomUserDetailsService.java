package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UserIDNotFoundException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Class for providing Spring Security with the user information from the database, and to add new users
 */
@Service
@AllArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    /**
     * Load a user from the database by their username
     * @param username the username identifying the user whose data is required.
     * @return Custom UserDetails object
     * @throws UsernameNotFoundException If the user does not exist in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(user);
    }

    /**
     * Function to load a User by their ID
     * @param id UUID of the user
     * @return UserDetails object
     */
    public UserDetails loadByUserID(UUID id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserIDNotFoundException(String.valueOf(id)));
        return new UserPrincipal(user);
    }

    /**
     * Method for adding a new user to the database
     * @param username Username for the new user
     * @param password Password for the new user. This will be encrypted with BCrypt
     * @throws UsernameExistsException If th
     */
    public void addUser(String username, String password) throws UsernameExistsException {
        User existingUser = userRepository.getUserByUsername(username);
        if (existingUser == null) {
            User.UserBuilder userBuilder = User.builder();
            userBuilder.id(UUID.randomUUID());
            userBuilder.username(username);
            userBuilder.passwordHash(passwordEncoder.encode(password));
            userRepository.save(userBuilder.build());
            log.info("User {} has been created", username);
        }else{
            log.info("User {} already exists", username);
            throw new UsernameExistsException(String.format("User %s already exists!", username));
        }
    }
}
