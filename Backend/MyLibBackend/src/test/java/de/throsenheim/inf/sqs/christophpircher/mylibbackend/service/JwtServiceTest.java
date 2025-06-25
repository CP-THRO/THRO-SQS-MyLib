package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.UUID;

import static javax.crypto.Cipher.SECRET_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private Environment environment;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).username("testuser").build();
    }

    @Test
    void generateTokenShouldCreateValidToken() {
        String token = jwtService.generateToken(user);
        assertNotNull(token);
    }

    @Test
    void extractUserIDShouldReturnCorrectUUID() {
        String token = jwtService.generateToken(user);
        UUID extracted = jwtService.extractUserID(token);
        assertEquals(user.getId(), extracted);
    }

    @Test
    void extractExpirationShouldReturnFutureDate() {
        String token = jwtService.generateToken(user);
        Date expiration = jwtService.extractExpiration(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(user);
        UserPrincipal principal = new UserPrincipal(user);
        assertTrue(jwtService.validateToken(token, principal));
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidUser() {
        String token = jwtService.generateToken(user);
        User differentUser = User.builder().id(UUID.randomUUID()).build();
        UserPrincipal wrongPrincipal = new UserPrincipal(differentUser);
        assertFalse(jwtService.validateToken(token, wrongPrincipal));
    }

    @Test
    void validateTokenShouldReturnFalseWhenExpired() {
        // Generate an expired token by setting expiration in the past
        Date expiredDate = new Date(System.currentTimeMillis() - 1000); // 1 second ago
        String expiredToken = jwtService.generateTokenWithCustomExpiration(user, expiredDate);

        boolean isValid = jwtService.validateToken(expiredToken, new UserPrincipal(user));

        assertFalse(isValid);
    }
}
