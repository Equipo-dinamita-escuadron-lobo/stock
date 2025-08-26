package com.stock.infrastructure.adapters.output.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String name;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean status;

    @UpdateTimestamp
    private Instant lastModifiedDate;

    @TenantId
    String tenantId;
}
