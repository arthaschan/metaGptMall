package com.arthaschan.metagptmall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String title;
    private String description;
    private Integer priceCents;
    private String currency;
    private Integer stock;
    private String imageUrl;
    private Boolean active;
    private LocalDateTime createdAt;
}
