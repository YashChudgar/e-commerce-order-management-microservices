package com.ecommerce.order_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.ecommerce.order_service.client.CustomerClient;
import com.ecommerce.order_service.client.ProductClient;
import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.enums.OrderStatus;
import com.ecommerce.order_service.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest request;
    private ProductResponse product;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {

        request = new CreateOrderRequest(
                1L,
                List.of(new OrderItemRequest(10L, 2))
        );

        product = new ProductResponse(
                10L,
                "Laptop",
                new BigDecimal("500.00"),
                10
        );

        orderResponse = new OrderResponse(
                1L,
                1L,
                OrderStatus.PENDING,
                new BigDecimal("1000.00"),
                null,
                null,
                List.of()
        );
    }

    // 1. Create order successfully
    @Test
    void createOrderSuccessfully() {

        when(productClient.getProduct(10L))
                .thenReturn(product);

        when(productClient.reserveStock(
                eq(10L), anyString(), eq(2)))
                .thenReturn(new ReservationResponse(
                        "reservation-123", 10L, 2, "RESERVED"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(1L, result.customerId());
        assertEquals(OrderStatus.PENDING, result.status());

        verify(customerClient).validateCustomer(1L);
        verify(productClient).getProduct(10L);
        verify(productClient).reserveStock(
                eq(10L), anyString(), eq(2));
        verify(orderRepository).save(any(Order.class));

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(new BigDecimal("1000.00"),
                savedOrder.getTotalAmount());

        assertEquals(1, savedOrder.getItems().size());
        assertEquals("Laptop",
                savedOrder.getItems().get(0).getProductName());
        assertEquals(2,
                savedOrder.getItems().get(0).getQuantity());
    }

    // 2. Create order with invalid customer
    @Test
    void createOrderWithInvalidCustomer() {

        doThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Customer not found"))
                .when(customerClient).validateCustomer(1L);

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request));

        verify(productClient, never()).getProduct(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 3. Create order with non-existing product
    @Test
    void createOrderWithInvalidProduct() {

        when(productClient.getProduct(10L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request));

        verify(productClient, never()).reserveStock(
                anyLong(), anyString(), anyInt());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 4. Create order with insufficient stock
    @Test
    void createOrderWithInsufficientStock() {

        ProductResponse unavailableProduct = new ProductResponse(
                10L,
                "Laptop",
                new BigDecimal("500.00"),
                1
        );

        when(productClient.getProduct(10L))
                .thenReturn(unavailableProduct);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(request)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));

        verify(productClient, never()).reserveStock(
                anyLong(), anyString(), anyInt());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 5. Create order with incomplete product details
    @Test
    void createOrderWithIncompleteProductDetails() {

        ProductResponse incompleteProduct = new ProductResponse(
                null,
                "Laptop",
                new BigDecimal("500.00"),
                10
        );

        when(productClient.getProduct(10L))
                .thenReturn(incompleteProduct);

        assertThrows(IllegalStateException.class,
                () -> orderService.createOrder(request));

        verify(productClient, never()).reserveStock(
                anyLong(), anyString(), anyInt());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 6. Release stock if order creation fails after reservation
    @Test
    void releaseStockWhenOrderCreationFails() {

        CreateOrderRequest multipleItemsRequest =
                new CreateOrderRequest(
                        1L,
                        List.of(
                                new OrderItemRequest(10L, 2),
                                new OrderItemRequest(20L, 1)
                        )
                );

        ProductResponse secondProduct = new ProductResponse(
                20L,
                "Phone",
                new BigDecimal("300.00"),
                5
        );

        when(productClient.getProduct(10L))
                .thenReturn(product);

        when(productClient.getProduct(20L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        when(productClient.reserveStock(
                eq(10L), anyString(), eq(2)))
                .thenReturn(new ReservationResponse(
                        "reservation-123", 10L, 2, "RESERVED"));

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(multipleItemsRequest));

        verify(productClient).releaseStock(
                10L, "reservation-123");

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 7. Get order by ID successfully
    @Test
    void getOrderByIdSuccessfully() {

        Order order = new Order();
        order.setCustomerId(1L);
        order.setTotalAmount(new BigDecimal("1000.00"));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.customerId());
        assertEquals(new BigDecimal("1000.00"),
                result.totalAmount());

        verify(orderRepository).findById(1L);
        verify(orderMapper).toResponse(order);
    }

    // 8. Get order that does not exist
    @Test
    void getOrderByIdNotFound() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND,
                exception.getStatusCode());

        verify(orderMapper, never()).toResponse(any(Order.class));
    }

    // 9. Get all orders successfully
    @Test
    void getAllOrdersSuccessfully() {

        Order order = new Order();
        order.setCustomerId(1L);

        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).customerId());

        verify(orderRepository).findAll();
        verify(orderMapper).toResponse(order);
    }

    // 10. Cancel pending order successfully
    @Test
    void cancelPendingOrderSuccessfully() {

        Order order = createPendingOrder();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(orderResponse);

        OrderResponse result = orderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(productClient).releaseStock(
                10L, "reservation-123");

        verify(orderRepository).save(order);
    }

    // 11. Cancel an already cancelled order
    @Test
    void cancelAlreadyCancelledOrder() {

        Order order = createPendingOrder();
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.cancelOrder(1L)
        );

        assertEquals(HttpStatus.CONFLICT,
                exception.getStatusCode());

        verify(productClient, never()).releaseStock(
                anyLong(), anyString());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 12. Cancel a confirmed order
    @Test
    void cancelConfirmedOrder() {

        Order order = createPendingOrder();
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.cancelOrder(1L)
        );

        assertEquals(HttpStatus.CONFLICT,
                exception.getStatusCode());

        verify(productClient, never()).releaseStock(
                anyLong(), anyString());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 13. Cancel an order that does not exist
    @Test
    void cancelOrderNotFound() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.cancelOrder(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND,
                exception.getStatusCode());

        verify(productClient, never()).releaseStock(
                anyLong(), anyString());
    }

    // Helper method to create a pending order
    private Order createPendingOrder() {

        Order order = new Order();
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));

        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setProductName("Laptop");
        item.setUnitPrice(new BigDecimal("500.00"));
        item.setQuantity(2);
        item.setLineTotal(new BigDecimal("1000.00"));
        item.setReservationId("reservation-123");

        order.addItem(item);

        return order;
    }
}

