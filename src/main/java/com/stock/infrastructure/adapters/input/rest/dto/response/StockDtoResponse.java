package com.stock.infrastructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockDtoResponse {
    private Long id;

    private Long productId;

    private Integer quantity;

    private Double price;

    private boolean status;   
}
