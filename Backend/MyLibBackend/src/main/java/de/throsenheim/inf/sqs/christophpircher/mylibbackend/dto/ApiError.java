package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * DTO for returning a more meaningful response on some errors.
 * Used in the ControllerExceptionHandler.
 */
@AllArgsConstructor
@NoArgsConstructor //For unit test
public class ApiError {
    @Getter
    @Schema(description = "The status code of the error response", example = "BAD_REQUEST")
    private HttpStatus status;
    @Getter
    @Schema(description = "The error message of the exception that caused this error", example = "This is an example error message")
    private String message;
    @Getter
    @Schema(description = "A list of all errors that caused the api error", example = "[\"error1\",\"error2\"]")
    private List<String> errors;

    public ApiError(HttpStatus status, String message, String error) {
        this(status, message, List.of(error));
    }
}