package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller to provide a simple health check.
 * Returns 200 as long as the application is running.
 */
@RestController
public class HealthController {

    @Operation(summary = "Health check", responses = {@ApiResponse(responseCode = "200", description = "Application is running")})
    @GetMapping("/api/v1/health")
    public ResponseEntity<Void> healthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
