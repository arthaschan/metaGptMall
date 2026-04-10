package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.PasswordResetRequest;
import com.ecommerce.auth.dto.PasswordResetResponse;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with this email does not exist");
        }

        // Hash new password
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());

        // Update password in database
        int updated = userRepository.updatePasswordHash(request.getEmail(), hashedPassword);
        if (updated == 0) {
            throw new RuntimeException("Failed to update password");
        }

        return new PasswordResetResponse("Password reset successful", request.getEmail());
    }
}
