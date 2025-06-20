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
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
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
    public UserDetails loadByUserID(UUID id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserIDNotFoundException(String.valueOf(id)));
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
