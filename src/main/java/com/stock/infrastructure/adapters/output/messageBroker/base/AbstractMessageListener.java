package com.stock.infrastructure.adapters.output.messageBroker.base;

import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase base abstracta para todos los message listeners de RabbitMQ.
 * Proporciona funcionalidad común para el manejo de mensajes sin lógica de DLQ.
 * 
 * @param <T> Tipo del evento/mensaje a procesar
 */
@Slf4j
public abstract class AbstractMessageListener<T> {

    /**
     * Método principal para manejar mensajes entrantes.
     * Implementa la lógica común de validación, procesamiento y acknowledgment.
     */
    protected void handleMessage(T event, Message message, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());
            
            if (!isValidEvent(event)) {
                log.warn("Invalid {} event received: {}", getEntityType(), event);
                acknowledgeMessage(channel, deliveryTag);
                return;
            }
            
            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());
            
        } catch (Exception e) {
            handleProcessingError(e, channel, deliveryTag);
        }
    }

    /**
     * Procesa el evento específico. Debe ser implementado por cada listener.
     */
    protected abstract void processEvent(T event);

    /**
     * Valida si el evento es válido para procesamiento.
     */
    protected abstract boolean isValidEvent(T event);

    /**
     * Retorna el tipo de entidad que maneja este listener (para logging).
     */
    protected abstract String getEntityType();

    /**
     * Maneja errores durante el procesamiento del mensaje.
     */
    private void handleProcessingError(Exception e, Channel channel, long deliveryTag) {
        try {
            log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);
            acknowledgeMessage(channel, deliveryTag); // ACK para evitar reenvío
        } catch (Exception ackException) {
            log.error("Error acknowledging message: {}", ackException.getMessage());
        }
    }

    /**
     * Envía acknowledgment del mensaje.
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", e.getMessage());
        }
    }

    /**
     * Obtiene un identificador seguro de la entidad para logging.
     * Método opcional que puede ser sobrescrito por listeners específicos.
     */
    protected String getEntityIdentifierSafely(T event) {
        return event != null ? event.toString() : "unknown";
    }

    /**
     * Método de utilidad para extraer contenido del mensaje como String.
     */
    protected String getMessageBodyAsString(Message message) {
        try {
            return new String(message.getBody());
        } catch (Exception e) {
            log.warn("Error converting message body to string: {}", e.getMessage());
            return "unavailable";
        }
    }
}
