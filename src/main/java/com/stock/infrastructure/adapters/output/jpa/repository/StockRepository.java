package com.stock.infrastructure.adapters.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

public interface StockRepository extends JpaRepository <StockEntity, Long> {

    
}
