package com.stock.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockDtoRequest {

    private Long productId;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean status = true;
}
