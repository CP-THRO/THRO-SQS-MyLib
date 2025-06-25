package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private static final String USERNAME = "johndoe";
    private static final String PASSWORDHASH = "dummypasswordhash";
    private static final String JWT = "mock-jwt-token";

    private final User dummyUser = User.builder()
            .username(USERNAME)
            .passwordHash(PASSWORDHASH)
            .build();

    @BeforeEach
    void setup() {
        reset(customUserDetailsService, jwtService, authenticationManager);
    }

    @Test
    void createNewUserShouldDelegateToUserService() throws UsernameExistsException {
        authService.createNewUser(USERNAME, PASSWORDHASH);

        verify(customUserDetailsService, times(1)).addUser(USERNAME, PASSWORDHASH);
    }

    @Test
    void authenticateShouldReturnJwtWhenAuthenticationSucceeds() {
        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authMock);
        when(customUserDetailsService.loadUserByUsername(USERNAME))
                .thenReturn(new UserPrincipal(dummyUser));
        when(jwtService.generateToken(dummyUser)).thenReturn(JWT);

        String result = authService.authenticate(USERNAME, PASSWORDHASH);

        assertEquals(JWT, result);
    }

    @Test
    void authenticateShouldThrowExceptionWhenAuthenticationFails() {
        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(authMock);

        UsernameNotFoundException thrown = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.authenticate(USERNAME, PASSWORDHASH)
        );

        assertEquals("Invalid username or password", thrown.getMessage());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticateShouldCallAuthenticationManagerWithCorrectToken() {
        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authMock);
        when(customUserDetailsService.loadUserByUsername(USERNAME))
                .thenReturn(new UserPrincipal(dummyUser));
        when(jwtService.generateToken(dummyUser)).thenReturn(JWT);

        authService.authenticate(USERNAME, PASSWORDHASH);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken token = captor.getValue();
        assertEquals(USERNAME, token.getPrincipal());
        assertEquals(PASSWORDHASH, token.getCredentials());
    }
}