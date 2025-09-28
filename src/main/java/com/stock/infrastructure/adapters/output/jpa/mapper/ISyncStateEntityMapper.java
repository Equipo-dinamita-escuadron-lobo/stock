package com.stock.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;

import com.stock.domain.model.SyncState;
import com.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;

@Mapper(componentModel = "spring")
public interface ISyncStateEntityMapper {
    SyncState toDomain(SyncStateEntity entity);
    
    SyncStateEntity toEntity(SyncState domain);
}
