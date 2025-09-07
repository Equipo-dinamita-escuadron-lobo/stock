package com.stock.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockSellDtoRequest {

    @NotNull(message = "The field 'productId' cannot be null")
    @Positive(message = "The productId must be positive")
    private Long productId;

    @NotNull(message = "The field 'quantity' cannot be null")
    @Positive(message = "The quantity must be positive")    
    private int quantity;
}
