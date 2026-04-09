package com.arthaschan.metagptmall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private Integer unitPriceCents;
    private LocalDateTime createdAt;
}
