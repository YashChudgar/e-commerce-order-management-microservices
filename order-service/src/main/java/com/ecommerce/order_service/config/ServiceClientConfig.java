package com.ecommerce.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration 
public class ServiceClientConfig {
    @Bean 
    public RestClient customerRestClient() {
        return RestClient.create("http://localhost:8081");
    }

    @Bean 
    public RestClient productRestClient(){
        return RestClient.create("http://localhost:8082");
    }
}
