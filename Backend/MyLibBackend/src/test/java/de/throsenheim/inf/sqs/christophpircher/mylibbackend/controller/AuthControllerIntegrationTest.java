package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private static final String ADD_USER_URL = "/api/v1/auth/add-user";
    private static final String AUTHENTICATE_URL = "/api/v1/auth/authenticate";

    private static final String AUTH_REQUEST = """
            {
               "username": "testuser",
               "password": "testpass"
            }
            """;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void addUserShouldReturn201() throws Exception {
        mockMvc.perform(post(ADD_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AUTH_REQUEST))
                .andExpect(status().isCreated());
    }



    @Test
    void addUserShouldReturn409WhenUsernameExists() throws Exception {
        AddUser();
        mockMvc.perform(post(ADD_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AUTH_REQUEST))
                .andExpect(status().isConflict());
    }



    @Test
    void authenticateShouldReturnToken() throws Exception {
        AddUser();
        mockMvc.perform(post(AUTHENTICATE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AUTH_REQUEST))
                .andExpect(status().isOk()).andReturn();
    }


    @Test
    void authenticateShouldReturn403WhenInvalidCredentials() throws Exception {

        mockMvc.perform(post(AUTHENTICATE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AUTH_REQUEST))
                .andExpect(status().isForbidden());
    }

    private void AddUser() throws Exception {
        mockMvc.perform(post(ADD_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AUTH_REQUEST))
                .andReturn();
    }

}