package com.ecommerce.order_service.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order_service.client.CustomerClient;
import com.ecommerce.order_service.client.ProductClient;
import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.dto.ProductResponse;
import com.ecommerce.order_service.dto.ReservationResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    public OrderService(
            OrderRepository orderRepository,
            CustomerClient customerClient,
            ProductClient productClient,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
        this.productClient = productClient;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        customerClient.validateCustomer(request.customerId());

        Order order = new Order();
        order.setCustomerId(request.customerId());

        BigDecimal total = BigDecimal.ZERO;
        List<ReservationResponse> reservations = new ArrayList<>();

        try {
            for (OrderItemRequest requestedItem : request.items()) {
                ProductResponse product = productClient.getProduct(requestedItem.productId());
                if (product == null || product.id() == null
                        || product.price() == null
                        || product.name() == null) {
                    throw new IllegalStateException(
                            "Product Service returned incomplete product details");
                }
                if (product.stockQuantity() == null
                        || product.stockQuantity() < requestedItem.quantity()) {
                    throw new IllegalArgumentException(
                            "Insufficient stock for product: " + product.name());
                }
                String reservationId = "ORDER-" + UUID.randomUUID();

                ReservationResponse reservation = productClient.reserveStock(
                        product.id(),
                        reservationId,
                        requestedItem.quantity());

                reservations.add(reservation);

                OrderItem item = new OrderItem();
                item.setProductId(product.id());
                item.setProductName(product.name());
                item.setUnitPrice(product.price());
                item.setQuantity(requestedItem.quantity());
                item.setLineTotal(
                        product.price().multiply(
                                BigDecimal.valueOf(requestedItem.quantity())));
                item.setReservationId(reservationId);

                order.addItem(item);
                total = total.add(item.getLineTotal());
            }

            order.setTotalAmount(total);

            Order savedOrder = orderRepository.save(order);
            return orderMapper.toResponse(savedOrder);

        } catch (RuntimeException exception) {
            for (ReservationResponse reservation : reservations) {
                try {
                    productClient.releaseStock(
                            reservation.productId(),
                            reservation.reservationId());
                } catch (RuntimeException releaseException) {
                    exception.addSuppressed(releaseException);
                }
            }

            throw exception;
        }
    }

    @Transactional
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + id));

        return orderMapper.toResponse(order);
    }

    @Transactional
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + id));

        if (order.getStatus() == com.ecommerce.order_service.enums.OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order has already been cancelled");
        }

        if (order.getStatus() != com.ecommerce.order_service.enums.OrderStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending orders can be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            if (item.getReservationId() != null) {
                productClient.releaseStock(
                        item.getProductId(),
                        item.getReservationId());
            }
        }

        order.setStatus(com.ecommerce.order_service.enums.OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }
}