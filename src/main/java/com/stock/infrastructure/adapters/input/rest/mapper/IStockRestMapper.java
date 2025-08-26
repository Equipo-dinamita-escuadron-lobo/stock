package com.stock.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.input.rest.dto.request.StockBuyDtoRequest;
import com.stock.infrastructure.adapters.input.rest.dto.request.StockSellDtoRequest;
import com.stock.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;

@Mapper(componentModel = "spring")
public interface IStockRestMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "enterpriseId", ignore = true)
    Stock toDomain(StockBuyDtoRequest stockDtoRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "enterpriseId", ignore = true)
    Stock toDomain(StockSellDtoRequest stockDtoRequest);

    StockDtoResponse toDtoResponse(Stock stock);
}
