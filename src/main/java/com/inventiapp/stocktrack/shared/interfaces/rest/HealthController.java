package com.inventiapp.stocktrack.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight health check endpoint for uptime monitoring tools.
 */
@RestController
@Tag(name = "Health", description = "Health Check Endpoints")
public class HealthController {

    /**
     * Returns a simple OK response to verify the application is running.
     * @return HTTP 200 with body "OK"
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Lightweight endpoint for uptime monitoring")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
