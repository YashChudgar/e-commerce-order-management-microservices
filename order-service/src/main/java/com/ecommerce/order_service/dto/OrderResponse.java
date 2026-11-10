package com.ecommerce.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.order_service.enums.OrderStatus;

public record OrderResponse(
    Long id,
    Long customerId,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderItemResponse> items
) {
}
