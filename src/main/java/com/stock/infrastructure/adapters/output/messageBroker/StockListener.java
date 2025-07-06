package com.stock.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.stock.application.ports.input.IStockCommandPort;
import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.config.RabbitConfig;
import com.stock.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.stock.infrastructure.adapters.output.messageBroker.dto.StockDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockListener{
    private final IStockCommandPort stockCommandPort;

    @RabbitListener(queues = RabbitConfig.PRODUCT_STOCK_QUEUE)
    public void handleStockEvent(EventDto<StockDto> event, Message message) {
        log.info("Received stock event: {}", event.getData());

        switch (event.getType()) {
            case CREATED:
                Stock stock = new Stock(event.getData().getProductId());
                stockCommandPort.save(stock);
                break;
            case UPDATED:
                
                break;
            case DELETED:
                
                break;
        }
    }
}