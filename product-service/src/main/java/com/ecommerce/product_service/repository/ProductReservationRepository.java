package com.ecommerce.product_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.product_service.entity.ProductReservation;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, String> {
    Optional<ProductReservation> findByReservationIdAndProductId(
            String reservationId,
            Long productId
    );  
} 