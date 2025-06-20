package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Class to parse an authentication request
 */
@Data
public class AuthRequestDTO {
    @JsonProperty("username")
    @Schema(description = "The username of the user", example = "imauser123")
    private String username;

    @JsonProperty("password")
    @Schema(description = "Password of the user", example = "Pa$$w0rd")
    private String password;
}
