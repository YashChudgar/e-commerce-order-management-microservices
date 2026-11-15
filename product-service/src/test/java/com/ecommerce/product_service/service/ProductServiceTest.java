package com.ecommerce.product_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("Test Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setStockQuantity(10);
    }

    // 1. Create product successfully
    @Test
    void createProductSuccessfully() {
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(product);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("999.99"), result.getPrice());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    // 2. Create product with blank name
    @Test
    void createProductWithBlankName() {
        product.setName(" ");

        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(product));

        verify(productRepository, never()).save(any(Product.class));
    }

    // 3. Create product with invalid price
    @Test
    void createProductWithInvalidPrice() {
        product.setPrice(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(product));

        verify(productRepository, never()).save(any(Product.class));
    }

    // 4. Create product with negative stock
    @Test
    void createProductWithNegativeStock() {
        product.setStockQuantity(-5);

        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(product));

        verify(productRepository, never()).save(any(Product.class));
    }

    // 5. Get product by ID successfully
    @Test
    void getProductByIdSuccessfully() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());

        verify(productRepository, times(1)).findById(1L);
    }

    // 6. Get product by ID when product does not exist
    @Test
    void getProductByIdNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(99L));

        verify(productRepository, times(1)).findById(99L);
    }

    // 7. Update product successfully
    @Test
    void updateProductSuccessfully() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("1199.99"));
        updatedProduct.setStockQuantity(20);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updatedProduct);

        assertEquals("Updated Laptop", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(new BigDecimal("1199.99"), result.getPrice());
        assertEquals(20, result.getStockQuantity());

        verify(productRepository, times(1)).save(product);
    }

    // 8. Update product that does not exist
    @Test
    void updateProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(99L, product));

        verify(productRepository, never()).save(any(Product.class));
    }

    // 9. Update product with invalid details
    @Test
    void updateProductWithInvalidPrice() {
        product.setPrice(new BigDecimal("-10"));

        assertThrows(IllegalArgumentException.class,
                () -> productService.updateProduct(1L, product));

        verify(productRepository, never()).save(any(Product.class));
    }

    // 10. Delete product successfully
    @Test
    void deleteProductSuccessfully() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }

    // 11. Delete product that does not exist
    @Test
    void deleteProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(99L));

        verify(productRepository, never()).delete(any(Product.class));
    }

    // 12. Search products without filters
    @Test
    void searchProductsWithoutFilters() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result =
                productService.searchProducts(null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    // 13. Search products by name
    @Test
    void searchProductsByName() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result =
                productService.searchProducts("laptop", null, null, null);

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    // 14. Search products by minimum price
    @Test
    void searchProductsByMinimumPrice() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result = productService.searchProducts(
                null, new BigDecimal("1000"), null, null);

        assertTrue(result.isEmpty());
    }

    // 15. Search products by maximum price
    @Test
    void searchProductsByMaximumPrice() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result = productService.searchProducts(
                null, null, new BigDecimal("1000"), null);

        assertEquals(1, result.size());
    }

    // 16. Search products that are in stock
    @Test
    void searchProductsInStock() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result =
                productService.searchProducts(null, null, null, true);

        assertEquals(1, result.size());
    }

    // 17. Search products that are out of stock
    @Test
    void searchProductsOutOfStock() {
        product.setStockQuantity(0);

        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result =
                productService.searchProducts(null, null, null, false);

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getStockQuantity());
    }

    // 18. Search products with no matching name
    @Test
    void searchProductsWithNoMatchingName() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result =
                productService.searchProducts("Mobile", null, null, null);

        assertTrue(result.isEmpty());
    }
}

