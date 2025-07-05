package com.stock.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stock {
    private Long id;

    private Long productId;

    private Integer quantity;

    private Double price;

    private boolean status;

    public Stock(Long productId, Integer quantity, Double price) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than 0");
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.status = true;
    }

    public void increaseStock(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        this.quantity += amount;
    }

    public void decreaseStock(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        if (this.quantity - amount < 0) throw new IllegalStateException("Not enough stock");
        this.quantity -= amount;
    }

    public void changePrice(double newPrice) {
        if (newPrice <= 0) throw new IllegalArgumentException("Price must be greater than 0");
        this.price = newPrice;
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
}
