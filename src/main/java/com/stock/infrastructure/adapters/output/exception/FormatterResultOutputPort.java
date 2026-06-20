package com.stock.infrastructure.adapters.output.exception;

import org.springframework.stereotype.Service;

import com.stock.domain.port.IFormatterResultOutputPort;
import com.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import com.stock.infrastructure.adapters.output.exception.customized.EntityAlreadyExists;
import com.stock.infrastructure.adapters.output.exception.customized.EntityDoesNotExistException;
import com.stock.infrastructure.adapters.output.exception.customized.ErrorCode;
import com.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;

@Service
public class FormatterResultOutputPort implements IFormatterResultOutputPort {

    @Override
    public void returnBusinessRuleErrorResponse(int status, String message) {
        throw new  BusinessRuleException(status, ErrorCode.BUSINESS_RULE_VIOLATION.getDescription()  + message);
    }

    @Override
    public void returnEntityAlreadyExistsErrorResponse(int status, String message) {
        throw new EntityAlreadyExists(status, ErrorCode.ENTITY_ALREADY_EXISTS.getDescription() + message);
    }

    @Override
    public void returnEntityDoesNotExistErrorResponse(int status, String message) {
        throw new EntityDoesNotExistException(status, ErrorCode.ENTITY_NOT_FOUND.getDescription() + message);
    }

    @Override
    public void returnErrorGenericResponse(int status, String message) {
        throw new GenericErrorException(status, ErrorCode.GENERIC_ERROR.getDescription() + message);
    }
    
}
