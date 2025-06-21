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


/**
 * REST controller that exposes authentication endpoints for user creation and login.
 * <p>
 * This controller interacts with the {@link AuthService} to manage user accounts
 * and issue JWT tokens upon successful authentication.
 * </p>
 *
 * <p>Exposed endpoints:</p>
 * <ul>
 *     <li><strong>POST /add-user</strong>: Register a new user</li>
 *     <li><strong>POST /authenticate</strong>: Authenticate and receive a JWT token</li>
 * </ul>
 *
 * <p>All routes are prefixed with <code>/api/v1/auth</code>.</p>
 *
 * @see AuthService
 * @see AuthRequestDTO
 * @see ApiError
 */
@RestController
@Tag(name = "Authentication")
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    /**
     * Creates a new user account using the provided username and password.
     * <p>
     * If the username already exists, a {@link UsernameExistsException} is thrown and a
     * 409 Conflict response is returned.
     * </p>
     *
     * @param newUser DTO containing the username and password
     * @return 201 Created response with no body
     * @throws UsernameExistsException if the username already exists in the system
     */
    @Operation(summary = "Create a new user", description = "Create a new user by providing the username and password", responses = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    @PostMapping("/add-user")
    public ResponseEntity<Void> addUser(@RequestBody AuthRequestDTO newUser) throws UsernameExistsException {
        authService.createNewUser(newUser.getUsername(),newUser.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Authenticates a user with the given credentials and returns a JWT token.
     * <p>
     * If authentication fails, a 403 Forbidden response is returned.
     * </p>
     *
     * @param authRequest DTO containing the username and password
     * @return 200 OK with a JWT token in the body
     * @throws UsernameNotFoundException if the provided credentials are invalid
     */
    @Operation(summary = "Authenticate a user by username and password", responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, Body contains the JWT token", content = @Content(schema = @Schema(implementation = String.class), examples = {@ExampleObject("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")})),
            @ApiResponse(responseCode = "403", description = "Authentication failed, Username or Password is incorrect"),
    })
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequestDTO authRequest) throws UsernameNotFoundException {
        return new ResponseEntity<>( authService.authenticate(authRequest.getUsername(), authRequest.getPassword()), HttpStatus.OK);
    }

}
