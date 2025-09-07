package com.stock.infrastructure.adapters.output.messageBroker.base;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.transaction.TransactionException;

import com.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import com.stock.infrastructure.adapters.output.exception.customized.EntityAlreadyExists;
import com.stock.infrastructure.adapters.output.exception.customized.EntityDoesNotExistException;
import com.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for RabbitMQ message listeners.
 * 
 * Provides common functionality for:
 * - Message processing with error handling
 * - Intelligent retry logic (domain vs infrastructure errors)
 * - Standardized logging
 * - Dead Letter Queue handling
 * 
 * @param <T> Event data type
 * @param <U> Event type enum
 */
@Slf4j
public abstract class AbstractMessageListener<T> {

    /**
     * Template method for handling incoming messages.
     * Implements the common flow: validate -> process -> acknowledge/reject
     */
    protected void handleMessage(
            T event,
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        try {
            // Safe check for the initial log  
            String entityIdentifier = getEntityIdentifierSafely(event);  
            log.info("Processing message with delivery tag: {} for {}: {}", 
                    deliveryTag, getEntityType(), entityIdentifier);

            // Event validation
            if (!isValidEvent(event)) {
                log.error("Received invalid event: {}", event);
                channel.basicNack(deliveryTag, false, false); // Send to DLQ
                return;
            }

            // Process the event (template method - implemented by subclasses)
            processEvent(event);

            // Manual message acknowledgment
            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed message for {}: {}", getEntityType(), entityIdentifier);

        } catch (Exception e) {
           // Safe check for the error log
            String entityIdentifier = getEntityIdentifierSafely(event);
            log.error("Error processing message for {}: {}, error: {}", 
                    getEntityType(), entityIdentifier, e.getMessage(), e);

            try {
                // Check if it is a domain/business logic error vs infrastructure error
                if (isRetryableError(e)) {
                    // Infrastructure error - reject and requeue for retry
                    channel.basicNack(deliveryTag, false, true);
                    log.warn("Infrastructure error detected. Retrying message for {}: {}. Error: {}", 
                            getEntityType(), entityIdentifier, e.getClass().getSimpleName());
                } else {
                    // Domain/Business rule error - send directly to DLQ (no retry)
                    channel.basicNack(deliveryTag, false, false);
                    log.error("Domain/Business rule violation detected. Sending message to DLQ for {}: {}. Error: {}", 
                            getEntityType(), entityIdentifier, e.getClass().getSimpleName());
                }
            } catch (IOException ioException) {
                log.error("Failed to nack message: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Determines if an error should trigger a retry.
     * 
     * Domain/Business exceptions are NOT retried as they will always fail.
     * Infrastructure errors are retried as they may be temporary.
     */
    private boolean isRetryableError(Exception e) {
        // Domain/Business exceptions should NOT be retried as they will always fail
        if (e instanceof BusinessRuleException ||
            e instanceof EntityDoesNotExistException ||
            e instanceof EntityAlreadyExists ||
            e instanceof GenericErrorException ||
            e instanceof IllegalArgumentException) {
            return false;
        }
        
        // Infrastructure errors that may be temporary and worth retrying
        return e instanceof DataAccessException ||
               e instanceof TransactionException ||
               e instanceof ConnectException ||
               e instanceof TimeoutException ||
               e instanceof org.springframework.amqp.AmqpException ||
               e instanceof java.sql.SQLException;
    }

    /**
     * Standard Dead Letter Queue handler for monitoring.
     */
    protected void handleDeadLetterQueue(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.error("Message sent to {} DLQ: {}", getEntityType(), messageBody);
            
        } catch (Exception e) {
            log.error("Error handling {} DLQ message: {}", getEntityType(), e.getMessage(), e);
        }
    }

    // Abstract methods to be implemented by subclasses

    /**
     * Process the specific business logic for the event.
     * @param event The event to process
     */
    protected abstract void processEvent(T event);

    /**
     * Validate if the event is valid for processing.
     * @param event The event to validate
     * @return true if valid, false otherwise
     */
    protected abstract boolean isValidEvent(T event);

    /**
     * Get a safe identifier for the entity being processed (for logging).
     * @param event The event
     * @return A string identifier
     */
    protected abstract String getEntityIdentifierSafely(T event);

    /**
     * Get the entity type name for logging purposes.
     * @return The entity type name (e.g., "Product", "Kardex")
     */
    protected abstract String getEntityType();
}
