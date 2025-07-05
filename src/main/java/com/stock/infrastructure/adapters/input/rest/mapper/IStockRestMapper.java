package com.stock.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;

import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.input.rest.dto.request.StockDtoRequest;
import com.stock.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;

@Mapper(componentModel = "spring")
public interface IStockRestMapper {
    Stock toDomain(StockDtoRequest stockDtoRequest);
    StockDtoResponse toDtoResponse(Stock stock);
}
