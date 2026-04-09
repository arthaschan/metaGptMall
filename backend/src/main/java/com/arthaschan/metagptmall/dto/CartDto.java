package com.arthaschan.metagptmall.dto;

import com.arthaschan.metagptmall.entity.CartItem;
import lombok.Data;

import java.util.List;

@Data
public class CartDto {
    private Long cartId;
    private List<CartItem> items;
}
