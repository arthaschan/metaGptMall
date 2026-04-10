package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.PasswordResetRequest;
import com.ecommerce.auth.dto.PasswordResetResponse;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("demo@example.com");
        testUser.setPasswordHash("old-hash");
        testUser.setRole("USER");

        validRequest = new PasswordResetRequest();
        validRequest.setEmail("demo@example.com");
        validRequest.setNewPassword("newpassword123");
        validRequest.setConfirmPassword("newpassword123");
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-hash");
        when(userRepository.updatePasswordHash(anyString(), anyString())).thenReturn(1);

        // Act
        PasswordResetResponse response = passwordResetService.resetPassword(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Password reset successful", response.getMessage());
        assertEquals("demo@example.com", response.getEmail());
        verify(userRepository).findByEmail("demo@example.com");
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).updatePasswordHash("demo@example.com", "new-hash");
    }

    @Test
    void resetPassword_PasswordsDoNotMatch_ThrowsException() {
        // Arrange
        validRequest.setConfirmPassword("different-password");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(validRequest));
        assertEquals("New password and confirmation do not match", exception.getMessage());
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        validRequest.setEmail("nonexistent@example.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(validRequest));
        assertEquals("User with this email does not exist", exception.getMessage());
    }

    @Test
    void resetPassword_UpdateFails_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-hash");
        when(userRepository.updatePasswordHash(anyString(), anyString())).thenReturn(0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword(validRequest));
        assertEquals("Failed to update password", exception.getMessage());
    }
}
