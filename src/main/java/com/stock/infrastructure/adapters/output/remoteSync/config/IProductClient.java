package com.stock.infrastructure.adapters.output.remoteSync.config;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import com.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;



/**
 * @brief HTTP client for product synchronization
 * 
 * Feign client interface for retrieving product data from remote services
 * for synchronization purposes.
 */
public interface IProductClient {
    
    /**
     * @brief Retrieves products by enterprise ID with optional time filter
     * @param enterpriseId The enterprise identifier
     * @param since Timestamp to filter products modified after this date
     * @return List of product synchronization DTOs
     */
    @GetExchange("/api/products/sync/findByEnterpriseId/{enterpriseId}")
    List<ProductSyncDto> findAllProductsByEnterpriseId(
        @PathVariable String enterpriseId,
        @RequestParam Instant since
    );
}
