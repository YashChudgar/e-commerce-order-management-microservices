package com.ecommerce.order_service.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ecommerce.order_service.dto.OrderItemResponse;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;

@Component 
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
