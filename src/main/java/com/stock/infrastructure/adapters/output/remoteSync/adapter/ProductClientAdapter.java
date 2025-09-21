package com.stock.infrastructure.adapters.output.remoteSync.adapter;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;


import com.stock.domain.model.Stock;
import com.stock.domain.port.IProductClientPort;
import com.stock.infrastructure.adapters.output.remoteSync.config.IProductClient;
import com.stock.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements IProductClientPort {

    private final IProductClientMapper productClientMapper;
    private final IProductClient productClient;

    @Override
    public List<Stock> findAllProductsByEnterpriseId(String enterpriseId, Instant since) {
        return productClient.findAllProductsByEnterpriseId(enterpriseId, since)
                .stream()
                .map(productClientMapper::toDomain)
                .toList();
    }
    
}
