package org.perfume.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.*;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication related endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse messageResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verifyUser(@Valid @RequestBody VerifyUserRequest request) {
        MessageResponse messageResponse = authService.verifyUser(request);
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationCode(@RequestParam String email) {
        MessageResponse messageResponse = authService.resendVerificationCode(email);
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        MessageResponse messageResponse= authService.initiatePasswordReset(request);
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<MessageResponse> verifyResetCode(
            @RequestParam String email,
            @RequestParam String resetCode) {
        MessageResponse messageResponse = authService.verifyResetCode(email, resetCode);
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        MessageResponse messageResponse = authService.resetPassword(request);
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(
            @RequestParam String email,
            @RequestParam String name) {
        AuthResponse authResponse = authService.googleAuth(email, name);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }


}
