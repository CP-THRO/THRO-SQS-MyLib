package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UserIDNotFoundException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private static final String USERNAME = "john_doe";
    private static final UUID USER_ID = UUID.randomUUID();

    private final User user = User.builder()
            .id(USER_ID)
            .username(USERNAME)
            .passwordHash("hashedPassword")
            .build();

    @Test
    void loadUserByUsernameShouldReturnUserPrincipalWhenUserExists() {
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(user);

        UserDetails details = userDetailsService.loadUserByUsername(USERNAME);

        assertInstanceOf(UserPrincipal.class, details);
        assertEquals(USERNAME, details.getUsername());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserNotFound() {
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(USERNAME);
        });
    }

    @Test
    void loadByUserIDShouldReturnUserPrincipalWhenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadByUserID(USER_ID);

        assertInstanceOf(UserPrincipal.class, details);
        assertEquals(USERNAME, details.getUsername());
    }

    @Test
    void loadByUserIDShouldThrowWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserIDNotFoundException.class, () -> {
            userDetailsService.loadByUserID(USER_ID);
        });
    }

    @Test
    void addUserShouldSaveUser_whenUsernameIsFree() throws UsernameExistsException {
        String rawPassword = "secure123";
        String encodedPassword = "encodedPassword";

        when(userRepository.getUserByUsername(USERNAME)).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        userDetailsService.addUser(USERNAME, rawPassword);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals(USERNAME, savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPasswordHash());
        assertNotNull(savedUser.getId());
    }

    @Test
    void addUserShouldThrowWhenUsernameExists() {
        when(userRepository.getUserByUsername(USERNAME)).thenReturn(user);

        assertThrows(UsernameExistsException.class, () -> {
            userDetailsService.addUser(USERNAME, "any");
        });

        verify(userRepository, never()).save(any());
    }
}