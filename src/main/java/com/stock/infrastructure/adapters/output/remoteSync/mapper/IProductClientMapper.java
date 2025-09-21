package com.stock.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

@Mapper(componentModel = "spring")
public interface IProductClientMapper {

    @Mapping(target = "presentation", ignore = true)
    @Mapping(target = "reference", ignore = true)
    ProductSyncDto toDto(Stock product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    Stock toDomain(ProductSyncDto dto);
}
