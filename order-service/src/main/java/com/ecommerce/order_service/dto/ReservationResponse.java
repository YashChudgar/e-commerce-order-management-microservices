package com.ecommerce.order_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservationResponse(
    String reservationId,
    Long productId,
    Integer quantity,
    String status
) {
    
}
