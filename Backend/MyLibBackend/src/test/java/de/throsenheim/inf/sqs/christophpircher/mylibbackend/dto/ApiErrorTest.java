package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void testConvenienceConstructorCreatesCorrectApiError() {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Validation failed";
        String error = "Field 'name' must not be null";

        ApiError apiError = new ApiError(status, message, error);

        assertEquals(status, apiError.getStatus());
        assertEquals(message, apiError.getMessage());
        assertNotNull(apiError.getErrors());
        assertEquals(1, apiError.getErrors().size());
        assertEquals(error, apiError.getErrors().getFirst());
    }
}