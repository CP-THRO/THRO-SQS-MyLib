package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.DummyController.DummyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DummyController.class)
@Import(ControllerExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ControllerExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String JSON_MESSAGE_FIELD = "$.message";

    @Test
    void handleIOExceptionShouldReturn502() throws Exception {
        mockMvc.perform(get("/dummy/io"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath(JSON_MESSAGE_FIELD ).value("Could not connect to external API: Downstream API error"));
    }

    @Test
    void handleUnexpectedStatusExceptionShouldReturn502() throws Exception {
        mockMvc.perform(get("/dummy/unexpected"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath(JSON_MESSAGE_FIELD ).value("Unexpected status"));
    }

    @Test
    void handleUsernameExistsExceptionShouldReturn409() throws Exception {
        mockMvc.perform(get("/dummy/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(JSON_MESSAGE_FIELD ).value("Username already exists"));
    }

    @Test
    void handleBookNotFoundExceptionShouldReturn404() throws Exception {
        mockMvc.perform(get("/dummy/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(JSON_MESSAGE_FIELD ).value("Book not found"));
    }

    @Test
    void handleBookNotInLibraryExceptionShouldReturn404() throws Exception {
        mockMvc.perform(get("/dummy/notinlibrary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(JSON_MESSAGE_FIELD ).value("Not in library"));
    }

    @Test
    void handleMethodArgumentNotValidShouldReturn400() throws Exception {
        DummyRequest invalidRequest = new DummyRequest();
        invalidRequest.setName("");

        mockMvc.perform(post("/dummy/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0]").exists());
    }


}

