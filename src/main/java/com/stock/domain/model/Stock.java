package com.stock.domain.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Stock {
    private Long id;

    private Long productId;

    private String enterpriseId;

    private String name;

    private int quantity;

    private BigDecimal price;

    private boolean status;

    public void activate() {
        this.status = true;
    }
    
    public void inactivate() {
        this.status = false;
    }

    public boolean isActive() {
        return this.status;
    }

    public void buy(int amount, BigDecimal price) {
        if (this.isActive() == false) {
            throw new IllegalStateException("Stock is not active");
        }
        if (amount < 0 || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount and price must be positive");
        }
        this.quantity += amount;
        this.price = price;
    }

    public void sell(int amount) {
        if (this.isActive() == false) {
            throw new IllegalStateException("Stock is not active");
        }
        if (amount < 0 || amount > this.quantity) {
            throw new IllegalArgumentException("Invalid amount for exit");
        }
        this.quantity -= amount;
    }
}
