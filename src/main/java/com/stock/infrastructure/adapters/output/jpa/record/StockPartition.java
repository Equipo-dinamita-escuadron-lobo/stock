package com.stock.infrastructure.adapters.output.jpa.record;

import java.util.List;

import com.stock.domain.model.Stock;

public record StockPartition(List<Stock> newProducts, List<Stock> existingProducts) {}
    
