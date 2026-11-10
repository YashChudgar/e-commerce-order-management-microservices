package com.ecommerce.product_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.product_service.entity.Product;

import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.From;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice
    );

    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
