package com.stock.infrastructure.adapters.output.jpa.entity;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Entity;
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
    private Long id;

    private Long productId;

    private Integer quantity;

    private Double price;

    private boolean status;

    @TenantId
    String tenantId;

}
