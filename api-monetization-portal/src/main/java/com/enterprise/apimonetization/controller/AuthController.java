package com.enterprise.apimonetization.controller;

import com.enterprise.apimonetization.dto.JwtResponse;
import com.enterprise.apimonetization.dto.LoginRequest;
import com.enterprise.apimonetization.dto.SignupRequest;
import com.enterprise.apimonetization.entity.User;
import com.enterprise.apimonetization.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for developer and admin onboarding and session management")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (default role: ROLE_DEVELOPER)")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        User user = authService.registerUser(signUpRequest);
        return ResponseEntity.ok("User registered successfully with ID: " + user.getId());
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and return a Bearer JWT Token")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }
}
