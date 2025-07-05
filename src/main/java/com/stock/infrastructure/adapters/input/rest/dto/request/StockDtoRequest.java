package com.stock.infrastructure.adapters.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockDtoRequest {
    @JsonIgnore
    private Long id;

    private Long productId;

    private Integer quantity;

    private Double price;

    private boolean status = true;
}
