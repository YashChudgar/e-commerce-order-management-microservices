package com.ecommerce.order_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component 
public class CustomerClient {
    private final RestClient customerRestClient;

    public CustomerClient(@Qualifier("customerRestClient") RestClient customerRestClient){
        this.customerRestClient = customerRestClient;
    }

    public void validateCustomer(Long customerId) {
        customerRestClient.get()
                    .uri("/api/customers/{id}",customerId)
                    .retrieve()
                    .toBodilessEntity();
    }
    
}
