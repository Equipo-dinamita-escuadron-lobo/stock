package com.stock.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Stock {
    private Long id;

    private Long productId;

    private Integer quantity;

    private Double price;

    private boolean status;

    public Stock(Long productId) {
        this.id = productId;
        this.productId = productId;
        this.quantity = 0;
        this.price = 0.0;
        this.status = true;
    }

    public void activate() {
        this.status = true;
    }
    
    public void inactivate() {
        this.status = false;
    }

    public boolean isActive() {
        return this.status;
    }

    public void buy(int amount, double price) {
        if (this.isActive() == false) {
            throw new IllegalStateException("Stock is not active");
        }
        if (amount < 0 || price < 0) {
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
