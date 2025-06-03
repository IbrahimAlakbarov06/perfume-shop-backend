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

    public UserResponse getUserById(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    public User findUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    public User findUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public List<UserResponse> getGoogleUsers() {
        return userDao.findByIsGoogleUserTrue()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(UserRole role) {
        return userDao.findByRole(role)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponse getUserByPhoneNumber(String phoneNumber) {
        User user = userDao.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User not found with phone number: " + phoneNumber));
        return userMapper.toDto(user);
    }

    public List<UserResponse> searchUsers(String searchTerm) {
        return userDao.findByNameOrEmailContaining(searchTerm, searchTerm)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getRecentlyRegisteredUsers() {
        return userDao.findRecentlyRegistered()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersWithActiveOrders() {
        return userDao.findUsersWithActiveOrders()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (existsByEmail(request.getEmail())) {
                throw new AlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
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

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidInputException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidInputException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);

        if (user.getRole() == UserRole.ADMIN) {
            throw new InvalidInputException("Cannot delete admin user");
        }

        userDao.delete(user);
    }

    public List<UserResponse> getAllUsers() {
        return userDao.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public User saveUser(User user) {
        return userDao.save(user);
    }

    @Transactional
    public void updateVerificationStatus(User user, boolean isVerified) {
        user.setVerified(isVerified);
        userDao.save(user);
    }

    @Transactional
    public UserResponse setUserRole(Long userId, UserRole role) {
        User user = findUserById(userId);
        user.setRole(role);
        User savedUser = userDao.save(user);
        return userMapper.toDto(savedUser);
    }
}