package com.arthaschan.metagptmall.dto;

import lombok.Data;

@Data
public class CreateOrderResponse {
    private Long orderId;
    private String orderNo;
    private Integer totalCents;
    private String currency;
    private String status;
}
