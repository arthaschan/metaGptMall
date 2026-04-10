package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.PasswordResetRequest;
import com.ecommerce.auth.dto.PasswordResetResponse;
import com.ecommerce.auth.service.PasswordResetService;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void resetPassword_Success() throws Exception {
        PasswordResetResponse response = new PasswordResetResponse("Password reset successful", "demo@example.com");
        when(passwordResetService.resetPassword(any(PasswordResetRequest.class))).thenReturn(response);

        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("demo@example.com");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"))
                .andExpect(jsonPath("$.email").value("demo@example.com"));
    }

    @Test
    void resetPassword_InvalidEmail() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("invalid-email");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_ShortPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("demo@example.com");
        request.setNewPassword("short");
        request.setConfirmPassword("short");

        mockMvc.perform(post("/api/auth/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
