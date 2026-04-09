package com.arthaschan.metagptmall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String role;
    private LocalDateTime createdAt;
}
