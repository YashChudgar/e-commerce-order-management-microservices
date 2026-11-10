package com.ecommerce.order_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ecommerce.order_service.dto.ProductResponse;
import com.ecommerce.order_service.dto.ReservationRequest;
import com.ecommerce.order_service.dto.ReservationResponse;

@Component 
public class ProductClient {
    private final RestClient productRestClient;

    public ProductClient(@Qualifier("productRestClient") RestClient productRestClient){
        this.productRestClient = productRestClient;
    }

    public ProductResponse getProduct(Long productId){
        return productRestClient.get()  
                    .uri("/api/products/{id}",productId)
                    .retrieve()
                    .body(ProductResponse.class);
    }

    public ReservationResponse reserveStock(
            Long productId,
            String reservationId,
            Integer quantity) {

        return productRestClient.post()
                .uri("/api/products/{productId}/reservations", productId)
                .body(new ReservationRequest(reservationId, quantity))
                .retrieve()
                .body(ReservationResponse.class);
    }

    public ReservationResponse releaseStock(
            Long productId,
            String reservationId) {

        return productRestClient.delete()
                .uri("/api/products/{productId}/reservations/{reservationId}",
                        productId, reservationId)
                .retrieve()
                .body(ReservationResponse.class);
    }
}