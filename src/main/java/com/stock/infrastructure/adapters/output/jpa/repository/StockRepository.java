package com.stock.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

public interface StockRepository extends JpaRepository <StockEntity, Long> {

    StockEntity findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    // Find all products by enterprise ID
    Collection<StockEntity> findAllByEnterpriseId(String enterpriseId);

    // Find all products by ID
    List<StockEntity> findByProductIdIn(List<Long> list);

    // Return a list of product IDs
    @Query("SELECT p.productId FROM StockEntity p WHERE p.productId IN :ids")
    List<Long> findProductsIdByProductIdIn(@Param("ids") List<Long> ids);

}
