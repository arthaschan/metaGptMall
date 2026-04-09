package com.arthaschan.metagptmall.security;

import lombok.Getter;

@Getter
public class AuthUser {
    private final Long id;
    private final String email;
    private final String role;

    public AuthUser(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }
}
