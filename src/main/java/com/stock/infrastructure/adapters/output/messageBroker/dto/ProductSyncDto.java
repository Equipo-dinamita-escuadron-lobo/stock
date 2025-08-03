package com.stock.infrastructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSyncDto {
    private Long productId;
    private String name;
    private String reference;
    private String enterpriseId;
}
