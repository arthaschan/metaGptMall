package com.arthaschan.metagptmall.service;

import com.arthaschan.metagptmall.dto.CreateOrderResponse;
import com.arthaschan.metagptmall.entity.*;
import com.arthaschan.metagptmall.exception.ApiException;
import com.arthaschan.metagptmall.mapper.*;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String TOPIC_TAG = "order.created:v1";

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final RocketMQTemplate rocketMQTemplate;

    public OrderService(CartMapper cartMapper,
                        CartItemMapper cartItemMapper,
                        ProductMapper productMapper,
                        OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        RocketMQTemplate rocketMQTemplate) {
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Transactional
    public CreateOrderResponse createOrder(Long userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            throw ApiException.badRequest("Cart is empty");
        }
        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw ApiException.badRequest("Cart is empty");
        }

        int totalCents = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : items) {
            Product product = productMapper.findById(ci.getProductId());
            if (product == null || !Boolean.TRUE.equals(product.getActive())) {
                throw ApiException.unprocessable("Product no longer available: " + ci.getProductId());
            }
            // Deduct stock with optimistic check
            int updated = productMapper.decreaseStock(ci.getProductId(), ci.getQuantity());
            if (updated == 0) {
                throw ApiException.unprocessable("Insufficient stock for product: " + product.getTitle());
            }
            int lineTotal = ci.getUnitPriceCents() * ci.getQuantity();
            totalCents += lineTotal;

            OrderItem oi = new OrderItem();
            oi.setProductId(ci.getProductId());
            oi.setTitleSnapshot(product.getTitle());
            oi.setUnitPriceCents(ci.getUnitPriceCents());
            oi.setQuantity(ci.getQuantity());
            orderItems.add(oi);
        }

        String orderNo = generateOrderNo();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus("CREATED");
        order.setTotalCents(totalCents);
        order.setCurrency("USD");
        orderMapper.insert(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
        }
        orderItemMapper.insertBatch(orderItems);

        // Clear cart
        cartItemMapper.deleteByCartId(cart.getId());

        // Send RocketMQ message (best-effort; failure should not roll back order)
        sendOrderCreatedEvent(order);

        CreateOrderResponse resp = new CreateOrderResponse();
        resp.setOrderId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        resp.setTotalCents(order.getTotalCents());
        resp.setCurrency(order.getCurrency());
        resp.setStatus(order.getStatus());
        return resp;
    }

    private void sendOrderCreatedEvent(Order order) {
        try {
            Map<String, Object> payload = Map.of(
                    "orderId", order.getId(),
                    "orderNo", order.getOrderNo(),
                    "userId", order.getUserId(),
                    "totalCents", order.getTotalCents(),
                    "currency", order.getCurrency(),
                    "createdAt", Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
            rocketMQTemplate.syncSend(TOPIC_TAG, MessageBuilder.withPayload(payload).build());
        } catch (Exception e) {
            log.warn("Failed to send RocketMQ message for order {}: {}", order.getOrderNo(), e.getMessage());
        }
    }

    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return ts + suffix;
    }
}
