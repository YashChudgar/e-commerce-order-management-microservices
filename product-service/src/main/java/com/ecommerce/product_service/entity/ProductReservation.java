package com.ecommerce.product_service.entity;

import java.time.LocalDateTime;

import com.ecommerce.product_service.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity 
@Table(name = "product_reservations")
public class ProductReservation {
    
    @Id
    @Column(length = 100, nullable = false, updatable = false) 
    private String reservationId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProductReservation() {
    }

    public ProductReservation(
            String reservationId,
            Long productId,
            Integer quantity,
            ReservationStatus status) {
        this.reservationId = reservationId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
