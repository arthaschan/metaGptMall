package com.arthaschan.metagptmall.controller;

import com.arthaschan.metagptmall.dto.CreateOrderResponse;
import com.arthaschan.metagptmall.security.AuthUser;
import com.arthaschan.metagptmall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management (requires authentication)")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create order from cart (deducts stock, clears cart, sends MQ event)")
    public ResponseEntity<CreateOrderResponse> createOrder(@AuthenticationPrincipal AuthUser authUser) {
        CreateOrderResponse resp = orderService.createOrder(authUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
