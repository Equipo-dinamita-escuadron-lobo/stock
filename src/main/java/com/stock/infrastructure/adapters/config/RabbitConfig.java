package com.stock.infrastructure.adapters.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;

@Configuration
@Slf4j
public class RabbitConfig {
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_STOCK_QUEUE = "product.stock.queue";
    
    // Dead Letter Queue configuration
    public static final String PRODUCT_STOCK_DLQ = "product.stock.dlq";
    public static final String PRODUCT_STOCK_DLX = "product.stock.dlx";
    
    // Retry configuration
    public static final String PRODUCT_STOCK_RETRY_QUEUE = "product.stock.retry.queue";

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

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

    // Retry Queue
    @Bean
    Queue productStockRetryQueue() {
        return QueueBuilder.durable(PRODUCT_STOCK_RETRY_QUEUE)
                .withArgument("x-message-ttl", 60000) // 1 minuto TTL
                .withArgument("x-dead-letter-exchange", PRODUCT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "")
                .build();
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

    @Bean
    Binding productStockRetryQueueBinding() {
        return BindingBuilder.bind(productStockRetryQueue()).to(productStockDlx());
    }

    // Custom RabbitTemplate with proper error handling
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        
        // Return callback para mensajes no enrutables
        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}", returned.getMessage());
            log.error("Reply code: {}", returned.getReplyCode());
            log.error("Reply text: {}", returned.getReplyText());
            log.error("Exchange: {}", returned.getExchange());
            log.error("Routing key: {}", returned.getRoutingKey());
        });
        
        // Confirm callback para publishers
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message not delivered to exchange. Cause: {}", cause);
            }
        });
        
        return template;
    }

    // Custom listener container factory
    @Bean
    RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}

