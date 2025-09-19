package com.stock.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.stock.domain.model.Stock;
import com.stock.domain.port.IStockCommandRepositoryPort;
import com.stock.infrastructure.adapters.config.RabbitProductConfig;
import com.stock.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import com.stock.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import com.stock.infrastructure.adapters.output.messageBroker.enums.EventStockType;
import com.stock.infrastructure.adapters.output.messageBroker.mapper.IProductBrokerMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockListener extends AbstractMessageListener<EventDto<ProductAsyncDto, EventStockType>> {
    private final IStockCommandRepositoryPort stockCommandPort;
    private final IProductBrokerMapper productBrokerMapper;

    @RabbitListener(queues = RabbitProductConfig.PRODUCT_STOCK_QUEUE)
    public void handleStockEvent(
            EventDto<ProductAsyncDto, EventStockType> event, 
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, message, channel, deliveryTag);
    }

    @Override
    public void processEvent(EventDto<ProductAsyncDto, EventStockType> event) {
        switch (event.getType()) {
            case CREATED:
                log.info("Creating new stock for product: {}", event.getData().getName());
                Stock stock = productBrokerMapper.toDomain(event.getData());
                stockCommandPort.save(stock);
                log.info("Stock created successfully for product: {}", event.getData().getName());
                break;
                
            case UPDATED:
                log.info("Updating stock for product: {}", event.getData().getName());
                Stock updatedStock = productBrokerMapper.toDomain(event.getData());
                stockCommandPort.update(updatedStock.getProductId(), updatedStock.getName());
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

    @Override
    protected boolean isValidEvent(EventDto<ProductAsyncDto, EventStockType> event) {
        return event != null && event.getType() != null && event.getData() != null
                && event.getData().getProductId() != null;
    }

    @Override
    protected String getEntityIdentifierSafely(EventDto<ProductAsyncDto, EventStockType> event) {
        if (event == null || event.getData() == null) {
            return "unknown";
        }

        String id = event.getData().getProductId().toString();
        return id != null ? id : "unknown";
    }

    @Override
    protected String getEntityType() {
        return "Stock";
    }

}
