package com.stock.infrastructure.adapters.config;


import lombok.RequiredArgsConstructor;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.stock.infrastructure.adapters.output.multitenancy.interceptor.TenantInterceptor;

@RequiredArgsConstructor
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
    }

    /**
     * @brief Creates a load-balanced WebClient builder
     * 
     * The @LoadBalanced annotation enables Spring Cloud to resolve
     * service names registered in Eureka (e.g., "lb://Name").
     *
     * @return Configured WebClient.Builder with load balancing
     */
    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

}