package com.stock.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

@Mapper(componentModel = "spring")
public interface StockEntityMapper {
    
    Stock toDomain(StockEntity entity);

    @Mapping(target = "tenantId", ignore = true)
    StockEntity toEntity(Stock domain);
}
