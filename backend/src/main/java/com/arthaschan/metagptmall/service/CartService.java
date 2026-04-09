package com.arthaschan.metagptmall.service;

import com.arthaschan.metagptmall.dto.AddCartItemRequest;
import com.arthaschan.metagptmall.dto.CartDto;
import com.arthaschan.metagptmall.entity.Cart;
import com.arthaschan.metagptmall.entity.CartItem;
import com.arthaschan.metagptmall.entity.Product;
import com.arthaschan.metagptmall.exception.ApiException;
import com.arthaschan.metagptmall.mapper.CartItemMapper;
import com.arthaschan.metagptmall.mapper.CartMapper;
import com.arthaschan.metagptmall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartService(CartMapper cartMapper, CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    private Cart getOrCreateCart(Long userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cartMapper.insert(cart);
        }
        return cart;
    }

    public CartDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setItems(items);
        return dto;
    }

    @Transactional
    public CartItem addItem(Long userId, AddCartItemRequest req) {
        Product product = productMapper.findById(req.getProductId());
        if (product == null || !Boolean.TRUE.equals(product.getActive())) {
            throw ApiException.notFound("Product not found: " + req.getProductId());
        }
        Cart cart = getOrCreateCart(userId);
        CartItem existing = cartItemMapper.findByCartIdAndProductId(cart.getId(), req.getProductId());
        if (existing != null) {
            int newQty = existing.getQuantity() + req.getQuantity();
            cartItemMapper.updateQuantity(existing.getId(), newQty);
            existing.setQuantity(newQty);
            return existing;
        }
        CartItem item = new CartItem();
        item.setCartId(cart.getId());
        item.setProductId(req.getProductId());
        item.setQuantity(req.getQuantity());
        item.setUnitPriceCents(product.getPriceCents());
        cartItemMapper.insert(item);
        return item;
    }
}
