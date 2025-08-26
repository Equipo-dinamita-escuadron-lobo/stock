package com.stock.application.ports.input;

public interface IStockStatusPort {
    void inactivate(Long id);
    void activate(Long id);
}
