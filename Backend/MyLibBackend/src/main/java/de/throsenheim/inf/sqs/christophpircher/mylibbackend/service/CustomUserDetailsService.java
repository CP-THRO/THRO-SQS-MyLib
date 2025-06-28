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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service class that integrates with Spring Security to load user data from the database.
 * <p>
 * Also provides a method to add new users to the system and to load users by their internal UUID.
 * </p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    /**
     * Loads a user from the database by their username.
     * <p>
     * This method is used by Spring Security for authentication.
     * </p>
     *
     * @param username the username identifying the user
     * @return a {@link UserDetails} implementation wrapping the {@link User}
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: '{}'", username);
        User user = userRepository.getUserByUsername(username);

        if (user == null) {
            log.warn("Username '{}' not found in database", username);
            throw new UsernameNotFoundException(username);
        }

        log.debug("User '{}' successfully loaded", username);
        return new UserPrincipal(user);
    }

    /**
     * Loads a user by their internal UUID.
     * <p>
     * This is useful when decoding JWT tokens that contain user IDs instead of usernames.
     * </p>
     *
     * @param id UUID of the user
     * @return a {@link UserDetails} instance for the user
     * @throws UserIDNotFoundException if no user with the given ID exists
     */
    public UserDetails loadByUserID(UUID id) {
        log.debug("Attempting to load user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User ID '{}' not found in database", id);
                    return new UserIDNotFoundException(String.valueOf(id));
                });

        log.debug("User with ID '{}' successfully loaded (username: '{}')", id, user.getUsername());
        return new UserPrincipal(user);
    }

    /**
     * Adds a new user to the system.
     * <p>
     * This method first checks if the username is already taken and encodes the password with BCrypt.
     * </p>
     *
     * @param username the new user's username
     * @param password the new user's plain-text password
     * @throws UsernameExistsException if the username already exists in the system
     */
    @Transactional
    public void addUser(String username, String password) throws UsernameExistsException {
        log.info("Registering new user: '{}'", username);

        User existingUser = userRepository.getUserByUsername(username);
        if (existingUser != null) {
            log.warn("Registration failed: user '{}' already exists", username);
            throw new UsernameExistsException(String.format("User %s already exists!", username));
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        log.info("User '{}' successfully created with ID '{}'", username, user.getId());
    }
}
