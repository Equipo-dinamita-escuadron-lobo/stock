package com.stock.infrastructure.adapters.output.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto<T> {
    private T data;
    private Integer errorCode;
    private String message;

    public ResponseEntity<ErrorResponseDto<T>> of() {
        return ResponseEntity.status(this.errorCode).body(this);
    }
}
