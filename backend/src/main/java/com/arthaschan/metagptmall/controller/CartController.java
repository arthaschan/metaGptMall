package com.arthaschan.metagptmall.controller;

import com.arthaschan.metagptmall.dto.AddCartItemRequest;
import com.arthaschan.metagptmall.dto.CartDto;
import com.arthaschan.metagptmall.entity.CartItem;
import com.arthaschan.metagptmall.security.AuthUser;
import com.arthaschan.metagptmall.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart (requires authentication)")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(cartService.getCart(authUser.getId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartItem> addItem(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AddCartItemRequest req) {
        CartItem item = cartService.addItem(authUser.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }
}
