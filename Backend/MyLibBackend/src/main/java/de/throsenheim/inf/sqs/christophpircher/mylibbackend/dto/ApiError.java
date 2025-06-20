package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a structured API error response.
 * <p>
 * This class is used by the {@link de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.ControllerExceptionHandler}
 * to return consistent and descriptive error information when exceptions occur.
 * </p>
 *
 * <p>It includes the HTTP status, a general message, and a list of specific error descriptions.</p>
 *
 * <p>This structure supports both single and multiple errors and is compatible with Swagger/OpenAPI documentation.</p>
 */
@AllArgsConstructor
@NoArgsConstructor //For unit test
public class ApiError {
    /**
     * The HTTP status of the error response.
     */
    @Getter
    @Schema(description = "The status code of the error response", example = "BAD_REQUEST")
    private HttpStatus status;

    /**
     * A general message describing the exception.
     */
    @Getter
    @Schema(description = "The error message of the exception that caused this error", example = "This is an example error message")
    private String message;

    /**
     * A list of individual error messages.
     * <p>Can be a single error or multiple field-level validation messages.</p>
     */
    @Getter
    @Schema(description = "A list of all errors that caused the api error", example = "[\"error1\",\"error2\"]")
    private List<String> errors;


    /**
     * Convenience constructor for a single error string.
     *
     * @param status  the HTTP status to return
     * @param message a high-level message describing the error
     * @param error   a single error detail to include in the list
     */
    public ApiError(HttpStatus status, String message, String error) {
        this(status, message, List.of(error));
    }
}