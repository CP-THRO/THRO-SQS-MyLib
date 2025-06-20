package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.ApiError;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.AuthRequestDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authentication")
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    /**
     * Endpoint for adding a new user to the database
     * @param newUser DTO object for the new user
     * @return Created resonse
     * @throws UsernameExistsException
     */
    @Operation(summary = "Create a new user", description = "Create a new user by providing the username and password", responses = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    @PostMapping("/add-user")
    public ResponseEntity<Void> addUser(@RequestBody AuthRequestDTO newUser) throws UsernameExistsException {
        authService.createNewUser(newUser.getUsername(),newUser.getPassword());
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Authenticate a user by username and password", responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, Body contains the JWT token", content = @Content(schema = @Schema(implementation = String.class), examples = {@ExampleObject("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")})),
            @ApiResponse(responseCode = "403", description = "Authentication failed, Username or Password is incorrect"),
    })
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequestDTO authRequest) throws UsernameNotFoundException {
        return new ResponseEntity<>( authService.authenticate(authRequest.getUsername(), authRequest.getPassword()), HttpStatus.OK);
    }

}
