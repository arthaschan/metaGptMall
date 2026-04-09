package com.arthaschan.metagptmall.entity;

import lombok.Data;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String titleSnapshot;
    private Integer unitPriceCents;
    private Integer quantity;
}
