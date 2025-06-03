package org.perfume.service;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.UserMapper;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // Get user by ID
    public UserResponse getUserById(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    // Get user by email
    public UserResponse getUserByEmail(String email) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    // Check if email exists
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    // Get user entity by email (for internal use)
    public User findUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    // Get user entity by ID (for internal use)
    public User findUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    // Get all Google users
    public List<UserResponse> getGoogleUsers() {
        return userDao.findByIsGoogleUserTrue()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get users by role
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userDao.findByRole(role)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get user by phone number
    public UserResponse getUserByPhoneNumber(String phoneNumber) {
        User user = userDao.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found with phone number: " + phoneNumber));
        return userMapper.toDto(user);
    }

    // Search users by name or email (Admin only)
    public List<UserResponse> searchUsers(String searchTerm) {
        return userDao.findByNameOrEmailContaining(searchTerm, searchTerm)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get recently registered users (Admin only)
    public List<UserResponse> getRecentlyRegisteredUsers() {
        return userDao.findRecentlyRegistered()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get users with active orders (Admin only)
    public List<UserResponse> getUsersWithActiveOrders() {
        return userDao.findUsersWithActiveOrders()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Update user profile
    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);

        // Check if new email is already taken by another user
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (existsByEmail(request.getEmail())) {
                throw new AlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
            // If email is changed, user needs to verify again
            user.setVerified(false);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User savedUser = userDao.save(user);
        return userMapper.toDto(savedUser);
    }

    // Update password
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = findUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidInputException("Current password is incorrect");
        }

        // Check if new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("New password and confirmation do not match");
        }

        // Check if new password is different from current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidInputException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
    }

    // Delete user (Admin only)
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);

        // Prevent deletion of admin users
        if (user.getRole() == UserRole.ADMIN) {
            throw new InvalidInputException("Cannot delete admin user");
        }

        userDao.delete(user);
    }

    // Get all users (Admin only)
    public List<UserResponse> getAllUsers() {
        return userDao.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Save user (for internal use)
    @Transactional
    public User saveUser(User user) {
        return userDao.save(user);
    }

    // Update user verification status
    @Transactional
    public void updateVerificationStatus(User user, boolean isVerified) {
        user.setVerified(isVerified);
        userDao.save(user);
    }

    // Set user role (Admin only)
    @Transactional
    public UserResponse setUserRole(Long userId, UserRole role) {
        User user = findUserById(userId);
        user.setRole(role);
        User savedUser = userDao.save(user);
        return userMapper.toDto(savedUser);
    }
}