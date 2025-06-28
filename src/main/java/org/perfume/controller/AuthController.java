package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.*;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.service.impl.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify user email")
    public ResponseEntity<MessageResponse> verifyUser(@Valid @RequestBody VerifyUserRequest request) {
        MessageResponse response = authService.verifyUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification code")
    public ResponseEntity<MessageResponse> resendVerificationCode(@RequestParam String email) {
        MessageResponse response = authService.resendVerificationCode(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        MessageResponse response = authService.initiatePasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    @Operation(summary = "Google OAuth login")
    public ResponseEntity<AuthResponse> googleAuth(@RequestParam String email, @RequestParam String name) {
        AuthResponse response = authService.googleAuth(email, name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal String email) {
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }
}