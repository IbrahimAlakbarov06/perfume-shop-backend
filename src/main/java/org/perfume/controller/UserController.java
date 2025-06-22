package org.perfume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.UserRole;
import org.perfume.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name ="User Management", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
        UserResponse userResponse = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        UserResponse userResponse = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/password")
    @Operation(summary = "Update user password")
    public ResponseEntity<MessageResponse> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        userService.updatePassword(user.getId(),request);
        return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete user account")
    public ResponseEntity<MessageResponse> deleteAccount(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        userService.deleteUser(user.getId());
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users (Admin only)")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String search) {
        List<UserResponse> users = userService.searchUsers(search);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (Admin only)")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin only)")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestBody UserRole role) {
        UserResponse user = userService.setUserRole(id, role);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }
}
