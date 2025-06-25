package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";
    private static final String TOKEN = "jwt-token";

    @Test
    void addUserShouldReturn201() throws Exception {
        doNothing().when(authService).createNewUser(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/add-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"username\": \"testuser\",
                                  \"password\": \"testpass\"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void addUserShouldReturn409WhenUsernameExists() throws Exception {
        doThrow(new UsernameExistsException("User already exists"))
                .when(authService).createNewUser(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/add-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"username\": \"testuser\",
                                  \"password\": \"testpass\"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void authenticateShouldReturnToken() throws Exception {
        when(authService.authenticate(TEST_USERNAME, TEST_PASSWORD)).thenReturn(TOKEN);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"username\": \"testuser\",
                                  \"password\": \"testpass\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(TOKEN));
    }

    @Test
    void authenticateShouldReturn403WhenInvalidCredentials() throws Exception {
        when(authService.authenticate(anyString(), anyString())).thenThrow(new UsernameNotFoundException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"username\": \"wrong\",
                                  \"password\": \"wrong\"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}