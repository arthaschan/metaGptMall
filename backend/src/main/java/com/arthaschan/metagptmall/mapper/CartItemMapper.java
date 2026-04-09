package com.arthaschan.metagptmall.mapper;

import com.arthaschan.metagptmall.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartItemMapper {
    List<CartItem> findByCartId(@Param("cartId") Long cartId);
    CartItem findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
    void insert(CartItem item);
    void updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);
    void deleteByCartId(@Param("cartId") Long cartId);
}
