package org.perfume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.perfume.domain.entity.User;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.UserMapper;
import org.perfume.model.dto.request.*;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.MessageResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.UserRole;
import org.perfume.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final long VERIFICATION_CODE_EXPIRY_MINUTES = 15;
    private static final long PASSWORD_RESET_CODE_EXPIRY_MINUTES = 30;

    // User registration
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userService.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.USER)
                .isVerified(false)
                .isGoogleUser(false)
                .build();

        // Generate and set verification code
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));

        // Save user
        User savedUser = userService.saveUser(user);

        // Send verification email
        sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), verificationCode);

        log.info("User registered successfully with email: {}", request.getEmail());
        return MessageResponse.of("Registration successful. Please check your email for verification code.");
    }

    // User login
    public AuthResponse login(LoginRequest request) {
        User user = userService.findUserByEmail(request.getEmail());

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidInputException("Invalid email or password");
        }

        // Check if user is verified
        if (!user.isVerified()) {
            throw new InvalidInputException("Please verify your email before logging in");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());
        UserResponse userResponse = userMapper.toDto(user);

        log.info("User logged in successfully: {}", request.getEmail());
        return new AuthResponse(token, userResponse);
    }

    // Verify user email
    @Transactional
    public MessageResponse verifyUser(VerifyUserRequest request) {
        User user = userService.findUserByEmail(request.getEmail());

        // Check if user is already verified
        if (user.isVerified()) {
            throw new InvalidInputException("User is already verified");
        }

        // Check verification code
        if (!user.isVerificationCodeValid()) {
            throw new InvalidInputException("Invalid or expired verification code");
        }

        if (!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new InvalidInputException("Invalid verification code");
        }

        // Verify user and clear verification code
        user.setVerified(true);
        user.clearVerificationCode();
        userService.saveUser(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        log.info("User verified successfully: {}", request.getEmail());
        return MessageResponse.of("Email verified successfully. Welcome to Perfume Shop!");
    }

    // Resend verification code
    @Transactional
    public MessageResponse resendVerificationCode(String email) {
        User user = userService.findUserByEmail(email);

        // Check if user is already verified
        if (user.isVerified()) {
            throw new InvalidInputException("User is already verified");
        }

        // Generate new verification code
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));

        userService.saveUser(user);

        // Send verification email
        sendVerificationEmail(user.getEmail(), user.getName(), verificationCode);

        log.info("Verification code resent to: {}", email);
        return MessageResponse.of("Verification code sent to your email");
    }

    // Send verification email
    public void sendVerificationEmail(String email, String name, String verificationCode) {
        String subject = "Verify Your Email - Perfume Shop";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Thank you for registering with Perfume Shop!\n\n" +
                        "Your verification code is: %s\n\n" +
                        "This code will expire in %d minutes.\n\n" +
                        "If you didn't create an account with us, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Perfume Shop Team",
                name, verificationCode, VERIFICATION_CODE_EXPIRY_MINUTES
        );

        emailService.sendEmail(email, subject, body);
    }

    // Generate verification code
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // Initiate password reset
    @Transactional
    public MessageResponse initiatePasswordReset(PasswordResetRequest request) {
        User user = userService.findUserByEmail(request.getEmail());

        // Generate reset code
        String resetCode = generateVerificationCode();
        user.setPasswordResetCode(resetCode);
        user.setPasswordResetCodeExpiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_CODE_EXPIRY_MINUTES));

        userService.saveUser(user);

        // Send password reset email
        sendPasswordResetEmail(user.getEmail(), user.getName(), resetCode);

        log.info("Password reset initiated for: {}", request.getEmail());
        return MessageResponse.of("Password reset code sent to your email");
    }

    // Verify reset code
    public MessageResponse verifyResetCode(String email, String resetCode) {
        User user = userService.findUserByEmail(email);

        if (!user.isPasswordResetCodeValid()) {
            throw new InvalidInputException("Invalid or expired reset code");
        }

        if (!user.getPasswordResetCode().equals(resetCode)) {
            throw new InvalidInputException("Invalid reset code");
        }

        return MessageResponse.of("Reset code verified successfully");
    }

    // Reset password
    @Transactional
    public MessageResponse resetPassword(PasswordResetConfirmRequest request) {
        User user = userService.findUserByEmail(request.getEmail());

        // Verify reset code
        if (!user.isPasswordResetCodeValid()) {
            throw new InvalidInputException("Invalid or expired reset code");
        }

        if (!user.getPasswordResetCode().equals(request.getResetCode())) {
            throw new InvalidInputException("Invalid reset code");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.clearPasswordResetCode();
        userService.saveUser(user);

        log.info("Password reset successfully for: {}", request.getEmail());
        return MessageResponse.of("Password reset successfully");
    }

    // Send password reset email
    public void sendPasswordResetEmail(String email, String name, String resetCode) {
        String subject = "Password Reset - Perfume Shop";
        String body = String.format(
                "Dear %s,\n\n" +
                        "You have requested to reset your password for your Perfume Shop account.\n\n" +
                        "Your password reset code is: %s\n\n" +
                        "This code will expire in %d minutes.\n\n" +
                        "If you didn't request a password reset, please ignore this email or contact support if you have concerns.\n\n" +
                        "Best regards,\n" +
                        "Perfume Shop Team",
                name, resetCode, PASSWORD_RESET_CODE_EXPIRY_MINUTES
        );

        emailService.sendEmail(email, subject, body);
    }

    // Google OAuth registration/login
    @Transactional
    public AuthResponse googleAuth(String email, String name) {
        User user;

        try {
            // Try to find existing user
            user = userService.findUserByEmail(email);

            // Update Google user flag if not set
            if (!user.isGoogleUser()) {
                user.setGoogleUser(true);
                user.setVerified(true); // Google users are automatically verified
                userService.saveUser(user);
            }
        } catch (NotFoundException e) {
            // Create new Google user
            user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode("GOOGLE_USER_" + System.currentTimeMillis())) // Random password
                    .role(UserRole.USER)
                    .isVerified(true)
                    .isGoogleUser(true)
                    .build();

            user = userService.saveUser(user);

            // Send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());

            log.info("New Google user registered: {}", email);
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());
        UserResponse userResponse = userMapper.toDto(user);

        log.info("Google user authenticated: {}", email);
        return new AuthResponse(token, userResponse);
    }

    // Get current user from token
    public UserResponse getCurrentUser(String email) {
        User user = userService.findUserByEmail(email);
        return userMapper.toDto(user);
    }
}