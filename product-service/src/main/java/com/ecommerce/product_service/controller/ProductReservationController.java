package com.ecommerce.product_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.product_service.entity.ProductReservation;
import com.ecommerce.product_service.service.ProductReservationService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController 
@RequestMapping("/api/products/{productId}/reservations")
@Validated 
public class ProductReservationController {
    private final ProductReservationService reservationService;

    public ProductReservationController(ProductReservationService reservationService){
        this.reservationService = reservationService;
    }

    public record ReservationRequest(
        @NotBlank (message="Reservation ID is required")
        @Size(max=100, message = "Reservation ID cannot exceed 100 characters")
        String reservationId,

        @Min (value = 1, message="Quantity must be at least 1")
        Integer quantity
    ){

    }

    @PostMapping
    public ResponseEntity<ProductReservation> reservationStock(
        @PathVariable Long productId,
        @RequestBody ReservationRequest request
    ) {
        if(request.quantity() == null || request.quantity() < 1){
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Quantity must be greater than zero"
            );
        }
        ProductReservation reservation = reservationService.reserveStock(
            productId,
            request.reservationId(),
            request.quantity());

        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ProductReservation> releaseStock(
            @PathVariable Long productId,
            @PathVariable String reservationId) {

        ProductReservation reservation = reservationService.releaseStock(
                productId, reservationId);

        return ResponseEntity.ok(reservation);
    }
    
}
