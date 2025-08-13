package com.stock.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;

@Mapper(componentModel = "spring")
public interface IProductBrokerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quantity", constant = "0")
    @Mapping(target = "price", constant = "0.0")
    @Mapping(target = "status", constant = "true")
    Stock toDomain(ProductAsyncDto productAsyncDto);
    
}
