package com.ecommerce.product_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;

import org.springframework.transaction.annotation.Transactional;    

@Service 
public class ProductService {
    
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @Transactional 
    public Product createProduct(Product product){
        validateProduct(product);
        product.setId(null);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id){
        return productRepository.findById(id)
            .orElseThrow(()->
            new ProductNotFoundException(
                "Product not found with id: " + id
            )
        );
    }

    @Transactional 
    public Product updateProduct(Long id, Product updatedProduct){
        validateProduct(updatedProduct);

        Product existingProduct = getProductById(id);

        existingProduct.setName(updatedProduct.getName().trim());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id){
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(
        String name,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock
    ){
        List<Product> products = productRepository.findAll();

        if (name != null && !name.isBlank()) {
            String searchName = name.trim().toLowerCase();

            products = products.stream()
                    .filter(product -> product.getName()
                            .toLowerCase()
                            .contains(searchName))
                    .toList();
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(product ->
                            product.getPrice().compareTo(minPrice) >= 0)
                    .toList();
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(product ->
                            product.getPrice().compareTo(maxPrice) <= 0)
                    .toList();
        }

        if (Boolean.TRUE.equals(inStock)) {
            products = products.stream()
                    .filter(product -> product.getStockQuantity() > 0)
                    .toList();
        } else if (Boolean.FALSE.equals(inStock)) {
            products = products.stream()
                    .filter(product -> product.getStockQuantity() == 0)
                    .toList();
        }

        return products;
    }

    private void validateProduct(Product product){
        if (product.getName() == null || product.getName().isBlank()){
            throw new IllegalArgumentException("Product name is required");
        }

        if(product.getName().trim().length() > 150){
            throw new IllegalArgumentException(
                "Product name cannot exceed 150 characters"
            );
        }

        if (product.getDescription() != null && product.getDescription().length() > 2000 ){
            throw new IllegalArgumentException(
                    "Description cannot exceed 2000 characters"
            );
        }

        if (product.getPrice() == null
                || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

         if (product.getStockQuantity() == null
                || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative"
            );
        }

        product.setName(product.getName().trim());
    }


}
