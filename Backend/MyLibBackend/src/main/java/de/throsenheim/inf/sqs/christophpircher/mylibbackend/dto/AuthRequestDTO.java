package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing an authentication request.
 * <p>
 * This object is used for both user registration and login endpoints, where
 * a client provides a username and password to authenticate or create an account.
 * </p>
 *
 * <p>Mapped to JSON using {@link JsonProperty} annotations and documented using Swagger's {@link Schema} annotations.</p>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.AuthController
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.AuthService
 */
@Data
public class AuthRequestDTO {
    /**
     * The username submitted by the client.
     */
    @JsonProperty("username")
    @Schema(description = "The username of the user", example = "imauser123")
    @NotBlank
    @NotEmpty
    @NotNull
    private String username;

    /**
     * The password submitted by the client.
     */
    @JsonProperty("password")
    @Schema(description = "Password of the user", example = "Pa$$w0rd")
    @NotBlank
    @NotEmpty
    @NotNull
    private String password;
}
