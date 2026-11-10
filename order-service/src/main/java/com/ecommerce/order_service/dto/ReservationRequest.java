package com.ecommerce.order_service.dto;

public record ReservationRequest (
    String reservationId,
    Integer quantity
) {
    
}
