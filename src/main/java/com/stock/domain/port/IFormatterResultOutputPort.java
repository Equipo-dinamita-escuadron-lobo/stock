package com.stock.domain.port;

public interface IFormatterResultOutputPort {
    public void returnResponseError(int status, String message);
}
