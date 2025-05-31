package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.UserDao;
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
import org.perfume.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final Random random = new SecureRandom();
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 15;
    private static final int PASSWORD_RESET_CODE_EXPIRY_MINUTES = 30;

    // İstifadəçi qeydiyyatı
    public MessageResponse register(RegisterRequest request) {
        // Email mövcudluğunu yoxla
        if (userDao.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // Telefon nömrəsi mövcudluğunu yoxla (əgər verilmişsə)
        if (request.getPhoneNumber() != null) {
            userDao.findByPhoneNumber(request.getPhoneNumber())
                    .ifPresent(user -> {
                        throw new AlreadyExistsException("User already exists with phone number: " + request.getPhoneNumber());
                    });
        }

        // Yeni istifadəçi yarat
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.USER)
                .isGoogleUser(false)
                .isVerified(false)
                .build();

        // Verification code yarat və göndər
        generateAndSetVerificationCode(user);

        User savedUser = userDao.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // Welcome email göndər
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

        // Verification email göndər
        sendVerificationEmail(savedUser);

        return MessageResponse.of("Registration successful! Please check your email to verify your account.");
    }

    // İstifadəçi login
    public AuthResponse login(LoginRequest request) {
        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Invalid email or password"));

        // Şifrəni yoxla
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidInputException("Invalid email or password");
        }

        // İstifadəçinin verify olunub-olunmadığını yoxla
        if (!user.isVerified()) {
            throw new InvalidInputException("Please verify your email before logging in");
        }

        // JWT token yarat
        String token = jwtService.generateToken(user.getEmail());
        UserResponse userResponse = userMapper.toDto(user);

        log.info("User logged in: {}", user.getEmail());
        return new AuthResponse(token, userResponse);
    }

    // İstifadəçini verify et
    public MessageResponse verifyUser(VerifyUserRequest request) {
        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + request.getEmail()));

        // İstifadəçi artıq verify olunubsa
        if (user.isVerified()) {
            throw new InvalidInputException("User is already verified");
        }

        // Verification code-u yoxla
        if (!user.isVerificationCodeValid()) {
            throw new InvalidInputException("Invalid or expired verification code");
        }

        if (!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new InvalidInputException("Invalid verification code");
        }

        // İstifadəçini verify et
        user.setVerified(true);
        user.clearVerificationCode();
        userDao.save(user);

        log.info("User verified: {}", user.getEmail());
        return MessageResponse.of("Email verification successful! You can now log in.");
    }

    // Verification code-u yenidən göndər
    public MessageResponse resendVerificationCode(String email) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // İstifadəçi artıq verify olunubsa
        if (user.isVerified()) {
            throw new InvalidInputException("User is already verified");
        }

        // Yeni verification code yarat və göndər
        generateAndSetVerificationCode(user);
        userDao.save(user);
        sendVerificationEmail(user);

        log.info("Verification code resent to: {}", email);
        return MessageResponse.of("Verification code sent to your email.");
    }

    // Şifrə yenilənməsini başlat
    public MessageResponse initiatePasswordReset(PasswordResetRequest request) {
        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + request.getEmail()));

        // Password reset code yarat
        generateAndSetPasswordResetCode(user);
        userDao.save(user);

        // Email göndər
        sendPasswordResetEmail(user);

        log.info("Password reset initiated for: {}", request.getEmail());
        return MessageResponse.of("Password reset code sent to your email.");
    }

    // Reset code-u verify et və şifrəni dəyiş
    public MessageResponse resetPassword(PasswordResetConfirmRequest request) {
        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + request.getEmail()));

        // Reset code-u yoxla
        if (!user.isPasswordResetCodeValid()) {
            throw new InvalidInputException("Invalid or expired reset code");
        }

        if (!user.getPasswordResetCode().equals(request.getResetCode())) {
            throw new InvalidInputException("Invalid reset code");
        }

        // Şifrəni yenilə
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.clearPasswordResetCode();
        userDao.save(user);

        log.info("Password reset successful for: {}", request.getEmail());
        return MessageResponse.of("Password reset successful! You can now log in with your new password.");
    }

    // Reset code-u verify et (şifrə dəyişdirmədən)
    public MessageResponse verifyResetCode(String email, String resetCode) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Reset code-u yoxla
        if (!user.isPasswordResetCodeValid()) {
            throw new InvalidInputException("Invalid or expired reset code");
        }

        if (!user.getPasswordResetCode().equals(resetCode)) {
            throw new InvalidInputException("Invalid reset code");
        }

        log.info("Reset code verified for: {}", email);
        return MessageResponse.of("Reset code is valid.");
    }

    // Helper metodlar
    private void generateAndSetVerificationCode(User user) {
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
    }

    private void generateAndSetPasswordResetCode(User user) {
        String resetCode = generateVerificationCode();
        user.setPasswordResetCode(resetCode);
        user.setPasswordResetCodeExpiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_CODE_EXPIRY_MINUTES));
    }

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void sendVerificationEmail(User user) {
        String subject = "Email Verification - Perfume Shop";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Thank you for registering with Perfume Shop!\n\n" +
                        "To complete your registration, please verify your email address using the verification code below:\n\n" +
                        "Verification Code: %s\n\n" +
                        "This code will expire in %d minutes.\n\n" +
                        "If you didn't create an account with us, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Perfume Shop Team",
                user.getName(),
                user.getVerificationCode(),
                VERIFICATION_CODE_EXPIRY_MINUTES
        );

        emailService.sendEmail(user.getEmail(), subject, body);
        log.info("Verification email sent to: {}", user.getEmail());
    }

    private void sendPasswordResetEmail(User user) {
        String subject = "Password Reset - Perfume Shop";
        String body = String.format(
                "Dear %s,\n\n" +
                        "We received a request to reset your password for your Perfume Shop account.\n\n" +
                        "Use the following code to reset your password:\n\n" +
                        "Reset Code: %s\n\n" +
                        "This code will expire in %d minutes.\n\n" +
                        "If you didn't request a password reset, please ignore this email or contact our support team.\n\n" +
                        "Best regards,\n" +
                        "Perfume Shop Team",
                user.getName(),
                user.getPasswordResetCode(),
                PASSWORD_RESET_CODE_EXPIRY_MINUTES
        );

        emailService.sendEmail(user.getEmail(), subject, body);
        log.info("Password reset email sent to: {}", user.getEmail());
    }
}