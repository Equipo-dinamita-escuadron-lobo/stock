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
    
    // Dead Letter Queue configuration
    public static final String PRODUCT_STOCK_DLQ = "product.stock.dlq";
    public static final String PRODUCT_STOCK_DLX = "product.stock.dlx";

    // Dead Letter Exchange
    @Bean
    FanoutExchange productStockDlx() {
        return new FanoutExchange(PRODUCT_STOCK_DLX, true, false);
    }

    // Dead Letter Queue
    @Bean
    Queue productStockDlq() {
        return QueueBuilder.durable(PRODUCT_STOCK_DLQ).build();
    }

    // Main Queue with DLQ configuration
    @Bean
    Queue productStockQueue() {
        return QueueBuilder.durable(PRODUCT_STOCK_QUEUE)
                .withArgument("x-dead-letter-exchange", PRODUCT_STOCK_DLX)
                .withArgument("x-dead-letter-routing-key", "")
                .build();
    }

    @Bean
    FanoutExchange productExchange() {
        return new FanoutExchange(PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    Binding productStockQueueBinding() {
        return BindingBuilder.bind(productStockQueue()).to(productExchange());
    }

    @Bean
    Binding productStockDlqBinding() {
        return BindingBuilder.bind(productStockDlq()).to(productStockDlx());
    }
}

