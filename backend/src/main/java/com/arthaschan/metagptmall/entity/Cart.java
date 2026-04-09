package com.arthaschan.metagptmall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Long id;
    private Long userId;
    private LocalDateTime createdAt;
}
