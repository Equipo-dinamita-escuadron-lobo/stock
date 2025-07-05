package com.stock.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stock.application.ports.input.IStockCommandPort;
import com.stock.application.ports.input.IStockQueryPort;
import com.stock.domain.model.Stock;
import com.stock.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.stock.infrastructure.adapters.input.rest.dto.request.StockDtoRequest;
import com.stock.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;
import com.stock.infrastructure.adapters.input.rest.mapper.IStockRestMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
public class StockController {
    
    private final IStockCommandPort stockCommandPort;
    private final IStockQueryPort stockQueryPort;
    private final IStockRestMapper stockRestMapper;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto<StockDtoResponse>> createStock(@RequestBody StockDtoRequest stockDtoRequest) {
        Stock stock = stockRestMapper.toDomain(stockDtoRequest);
        Stock createdStock = stockCommandPort.save(stock);
        StockDtoResponse stockDtoResponse = stockRestMapper.toDtoResponse(createdStock);
        return ResponseDto.<StockDtoResponse>builder()
                .data(stockDtoResponse)
                .status(201)
                .message("Stock created successfully")
                .build()
                .of();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<StockDtoResponse>> getStockById(@PathVariable Long id) {
        Stock stock = stockQueryPort.findById(id);
        StockDtoResponse stockDtoResponse = stockRestMapper.toDtoResponse(stock);
        return ResponseDto.<StockDtoResponse>builder()
                .data(stockDtoResponse)
                .status(200)
                .message("Stock retrieved successfully")
                .build()
                .of();   
        }
    }
