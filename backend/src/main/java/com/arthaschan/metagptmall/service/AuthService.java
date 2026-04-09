package com.arthaschan.metagptmall.service;

import com.arthaschan.metagptmall.dto.LoginRequest;
import com.arthaschan.metagptmall.dto.LoginResponse;
import com.arthaschan.metagptmall.dto.RegisterRequest;
import com.arthaschan.metagptmall.entity.User;
import com.arthaschan.metagptmall.exception.ApiException;
import com.arthaschan.metagptmall.mapper.UserMapper;
import com.arthaschan.metagptmall.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest req) {
        if (userMapper.findByEmail(req.getEmail()) != null) {
            throw ApiException.conflict("Email already registered");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("user");
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.findByEmail(req.getEmail());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        return LoginResponse.of(token, jwtUtil.getExpirationMs());
    }
}
