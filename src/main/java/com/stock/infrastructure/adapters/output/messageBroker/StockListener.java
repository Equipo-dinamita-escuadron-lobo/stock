package com.stock.infrastructure.adapters.output.messageBroker;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import com.stock.domain.model.Stock;
import com.stock.domain.port.IStockCommandRepositoryPort;
import com.stock.infrastructure.adapters.config.RabbitConfig;
import com.stock.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.stock.infrastructure.adapters.output.messageBroker.dto.ProductSyncDto;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockListener{
    private final IStockCommandRepositoryPort stockCommandPort;

    @RabbitListener(queues = RabbitConfig.PRODUCT_STOCK_QUEUE)
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleStockEvent(
            EventDto<ProductSyncDto> event, 
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            // Safe check for the initial log
            String productName = getProductNameSafely(event);
            log.info("Processing message with delivery tag: {} for product: {}", 
                    deliveryTag, productName);

            // Event validation
            if (event == null || event.getData() == null) {
                log.error("Received null event or null event data");
                channel.basicNack(deliveryTag, false, false); // Send to DLQ
                return;
            }

            // Process the event
            processEvent(event);
            
            // Manual message acknowledgment
            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed and acknowledged message for product: {}", 
                    event.getData().getName());

        } catch (Exception e) {
            // Safe check for the error log
            String productName = getProductNameSafely(event);
            log.error("Error processing message for product: {}, error: {}", 
                    productName, e.getMessage(), e);
            
            try {
                // Check if it is a parsing or business logic error
                if (isRetryableError(e)) {
                    // Reject and requeue for retry
                    channel.basicNack(deliveryTag, false, true);
                    log.warn("Retrying message for product: {}", productName);
                } else {
                    // Non-recoverable error, send to DLQ
                    channel.basicNack(deliveryTag, false, false);
                    log.error("Sending message to DLQ for product: {}", productName);
                }
            } catch (IOException ioException) {
                log.error("Failed to nack message: {}", ioException.getMessage());
            }
        }
    }

    private void processEvent(EventDto<ProductSyncDto> event) {
        switch (event.getType()) {
            case CREATED:
                log.info("Creating new stock for product: {}", event.getData().getName());
                Stock stock = new Stock(event.getData().getProductId());
                stockCommandPort.save(stock);
                log.info("Stock created successfully for product: {}", event.getData().getName());
                break;
                
            case UPDATED:
                log.info("Updating stock for product: {}", event.getData().getName());
                Stock updatedStock = new Stock(event.getData().getProductId());
                stockCommandPort.save(updatedStock);
                log.info("Stock updated successfully for product: {}", event.getData().getName());
                break;
                
            case DELETED:
                log.info("Deleting stock for product: {}", event.getData().getName());
                log.info("Stock deletion processed for product: {}", event.getData().getProductId());
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }
    }

    private boolean isRetryableError(Exception e) {
        // Errors that may be temporary and worth retrying
        return e instanceof DataAccessException ||
               e instanceof TransactionException ||
               e instanceof ConnectException ||
               e instanceof TimeoutException;
    }

    // Listener for the Dead Letter Queue - for monitoring
    @RabbitListener(queues = RabbitConfig.PRODUCT_STOCK_DLQ)
    public void handleDeadLetterQueue(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.error("Message sent to DLQ: {}", messageBody);
            
        } catch (Exception e) {
            log.error("Error handling DLQ message: {}", e.getMessage(), e);
        }
    }

    // Helper method to get the product name safely
    private String getProductNameSafely(EventDto<ProductSyncDto> event) {
        if (event == null || event.getData() == null) {
            return "unknown";
        }
        
        String name = event.getData().getName();
        return name != null ? name : "unnamed";
    }
}