package com.stock.infrastructure.adapters.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
@Slf4j
public class RabbitProductConfig {
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_STOCK_QUEUE = "product.stock.queue";

    @Bean
    Queue productStockQueue() {
        return QueueBuilder.durable(PRODUCT_STOCK_QUEUE).build();
    }

    @Bean
    FanoutExchange productExchange() {
        return new FanoutExchange(PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    Binding productStockQueueBinding() {
        return BindingBuilder.bind(productStockQueue()).to(productExchange());
    }

}

