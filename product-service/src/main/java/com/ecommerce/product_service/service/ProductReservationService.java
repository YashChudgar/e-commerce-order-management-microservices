package com.ecommerce.product_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.entity.ProductReservation;
import com.ecommerce.product_service.enums.ReservationStatus;
import com.ecommerce.product_service.exception.InsufficientStockException;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.exception.ReservationNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;
import com.ecommerce.product_service.repository.ProductReservationRepository;

@Service
public class ProductReservationService {

    private final ProductRepository productRepository;
    private final ProductReservationRepository reservationRepository;

    public ProductReservationService(ProductRepository productRepository,
            ProductReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ProductReservation reserveStock(Long productId, String reservationId, Integer quantity) {
        validateReservationInput(reservationId, quantity);

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        var existingReservation = reservationRepository.findById(reservationId);

        if (existingReservation.isPresent()) {
            ProductReservation existing = existingReservation.get();

            if (!existing.getProductId().equals(productId) || !existing.getQuantity().equals(quantity)) {
                throw new IllegalArgumentException("Reservation ID already exists with different details");
            }

            if (existing.getStatus() == ReservationStatus.RELEASED) {
                throw new IllegalArgumentException(
                        "This reservation has already been released");
            }

            return existing;
        }

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: "
                            + product.getStockQuantity()
                            + ", requested: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        ProductReservation reservation = new ProductReservation(
                reservationId,
                productId,
                quantity,
                ReservationStatus.ACTIVE);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public ProductReservation releaseStock(
            Long productId,
            String reservationId) {

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        ProductReservation reservation = reservationRepository
                .findByReservationIdAndProductId(reservationId, productId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return reservation;
        }

        product.setStockQuantity(
                product.getStockQuantity() + reservation.getQuantity());

        productRepository.save(product);

        reservation.setStatus(ReservationStatus.RELEASED);
        return reservationRepository.save(reservation);
    }

    private void validateReservationInput(
            String reservationId,
            Integer quantity) {

        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Reservation ID is required");
        }

        if (reservationId.length() > 100) {
            throw new IllegalArgumentException(
                    "Reservation ID cannot exceed 100 characters");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be greater than zero");
        }
    }
}
