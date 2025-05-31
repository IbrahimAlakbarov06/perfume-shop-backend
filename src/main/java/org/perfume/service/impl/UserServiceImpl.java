package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // USER və ADMIN üçün - öz profilini əldə etmək
    public UserResponse getCurrentUser(String email) {
        User user = findUserByEmail(email);
        log.info("Current user retrieved: {}", email);
        return userMapper.toDto(user);
    }

    // USER və ADMIN üçün - profil məlumatlarını yeniləmək
    public UserResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = findUserByEmail(email);

        // Emailin digər istifadəçi tərəfindən istifadə olunub-olunmadığını yoxla
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userDao.existsByEmail(request.getEmail())) {
                throw new AlreadyExistsException("Email address is already in use");
            }
            user.setEmail(request.getEmail());
            user.setVerified(false); // Email dəyişdikdə yenidən verify lazımdır
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhoneNumber() != null) {
            // Telefon nömrəsinin digər istifadəçi tərəfindən istifadə olunub-olunmadığını yoxla
            userDao.findByPhoneNumber(request.getPhoneNumber())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new AlreadyExistsException("Phone number is already in use");
                        }
                    });
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userDao.save(user);
        log.info("User profile updated: {}", email);
        return userMapper.toDto(updatedUser);
    }

    // USER və ADMIN üçün - şifrəni dəyişdirmək
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = findUserByEmail(email);

        // Hazırkı şifrəni yoxla
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidInputException("Current password is incorrect");
        }

        // Yeni şifrə və təsdiq şifrəsinin eyni olduğunu yoxla
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("New password and confirm password do not match");
        }

        // Yeni şifrənin hazırkı şifrədən fərqli olduğunu yoxla
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidInputException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
        log.info("Password updated for user: {}", email);
    }

    // ADMIN üçün - istifadəçi məlumatı
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        log.info("User retrieved by ID: {}", id);
        return userMapper.toDto(user);
    }

    // ADMIN üçün - istifadəçini silmək
    public void deleteUser(Long id) {
        User user = findUserById(id);

        // Admin özünü silə bilməz
        if (user.getRole() == UserRole.ADMIN) {
            throw new InvalidInputException("Cannot delete admin user");
        }

        userDao.delete(user);
        log.info("User deleted: {}", id);
    }

    // ADMIN üçün - rola görə istifadəçiləri tapmaq
    public List<UserResponse> getUsersByRole(UserRole role) {
        List<User> users = userDao.findByRole(role);
        log.info("Users found by role {}: {}", role, users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // ADMIN üçün - Google istifadəçilərini tapmaq
    public List<UserResponse> getGoogleUsers() {
        List<User> users = userDao.findByIsGoogleUserTrue();
        log.info("Google users found: {}", users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // ADMIN üçün - istifadəçi axtarışı
    public List<UserResponse> searchUsers(String searchTerm) {
        List<User> users = userDao.findByNameOrEmailContaining(searchTerm, searchTerm);
        log.info("Users found by search term '{}': {}", searchTerm, users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // ADMIN üçün - son qeydiyyat olanlar
    public List<UserResponse> getRecentlyRegisteredUsers() {
        List<User> users = userDao.findRecentlyRegistered();
        log.info("Recently registered users found: {}", users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // ADMIN üçün - aktiv sifarişi olan istifadəçilər
    public List<UserResponse> getUsersWithActiveOrders() {
        List<User> users = userDao.findUsersWithActiveOrders();
        log.info("Users with active orders found: {}", users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // PUBLIC üçün - email mövcudluğunu yoxlamaq
    public boolean isEmailExists(String email) {
        boolean exists = userDao.existsByEmail(email);
        log.info("Email existence check for '{}': {}", email, exists);
        return exists;
    }

    // Daxili helper metodlar
    private User findUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private User findUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    // AuthService üçün helper metod
    public User getUserByEmail(String email) {
        return findUserByEmail(email);
    }

    // AuthService üçün helper metod
    public User saveUser(User user) {
        return userDao.save(user);
    }
}