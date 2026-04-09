package com.arthaschan.metagptmall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private String status;
    private Integer totalCents;
    private String currency;
    private LocalDateTime createdAt;
}
