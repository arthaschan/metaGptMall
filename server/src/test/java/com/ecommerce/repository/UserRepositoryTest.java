package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_UserExists_ReturnsUser() {
        // Arrange - data.sql inserts a demo user with email 'demo@example.com'

        // Act
        Optional<User> result = userRepository.findByEmail("demo@example.com");

        // Assert
        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals("demo@example.com", user.getEmail());
        assertEquals("USER", user.getRole());
        assertNotNull(user.getPasswordHash());
    }

    @Test
    void findByEmail_UserDoesNotExist_ReturnsEmpty() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void updatePasswordHash_Success() {
        // Arrange
        String newPasswordHash = "$2a$10$newHashGeneratedForTest";
        
        // Act
        int updated = userRepository.updatePasswordHash("demo@example.com", newPasswordHash);
        
        // Assert
        assertEquals(1, updated);
        
        // Verify the update worked
        Optional<User> user = userRepository.findByEmail("demo@example.com");
        assertTrue(user.isPresent());
        assertEquals(newPasswordHash, user.get().getPasswordHash());
    }

    @Test
    void updatePasswordHash_UserDoesNotExist_ReturnsZero() {
        // Act
        int updated = userRepository.updatePasswordHash("nonexistent@example.com", "new-hash");
        
        // Assert
        assertEquals(0, updated);
    }
}
