package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USERNAME = "testuser";
    private static final String PASSWORD_HASH = "$2a$10$example";

    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .passwordHash(PASSWORD_HASH)
                .build();

        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void getUsernameShouldReturnUsername() {
        assertEquals(USERNAME, userPrincipal.getUsername());
    }

    @Test
    void getPasswordShouldReturnPasswordHash() {
        assertEquals(PASSWORD_HASH, userPrincipal.getPassword());
    }

    @Test
    void getUserIDShouldReturnUUID() {
        assertEquals(USER_ID, userPrincipal.getUserID());
    }

    @Test
    void getAuthoritiesShouldReturnEmptyList() {
        assertNotNull(userPrincipal.getAuthorities());
        assertTrue(userPrincipal.getAuthorities().isEmpty());
    }

    @Test
    void getUserShouldReturnWrappedUser() {
        assertEquals(user, userPrincipal.getUser());
    }
}
