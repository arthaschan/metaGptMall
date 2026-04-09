package com.arthaschan.metagptmall.dto;

import com.arthaschan.metagptmall.entity.Product;
import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String title;
    private String description;
    private Integer priceCents;
    private String currency;
    private Integer stock;
    private String imageUrl;
    private Boolean active;

    public static ProductDto from(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setPriceCents(p.getPriceCents());
        dto.setCurrency(p.getCurrency());
        dto.setStock(p.getStock());
        dto.setImageUrl(p.getImageUrl());
        dto.setActive(p.getActive());
        return dto;
    }
}
