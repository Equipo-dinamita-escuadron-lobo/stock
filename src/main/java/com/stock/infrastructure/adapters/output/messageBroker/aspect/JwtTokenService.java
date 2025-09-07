package com.stock.infrastructure.adapters.output.messageBroker.aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stock.infrastructure.adapters.output.security.IJwtUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio unificado para gestionar tokens JWT tanto desde el contexto HTTP 
 * como desde mensajes de RabbitMQ.
 */
@Service
@Slf4j
public class JwtTokenService {

    @Autowired
    private IJwtUtils jwtUtils;

    // ThreadLocal para tokens de RabbitMQ
    private static final ThreadLocal<String> rabbitJwtToken = new ThreadLocal<>();
    private static final ThreadLocal<String> rabbitTenantId = new ThreadLocal<>();

    /**
     * Establece el token JWT para el contexto de RabbitMQ.
     * Se utiliza cuando se recibe un mensaje de RabbitMQ.
     */
    public void setRabbitJwtToken(String token) {
        rabbitJwtToken.set(token);
        log.debug("Token JWT establecido para contexto RabbitMQ");
    }

    /**
     * Establece el tenant ID para el contexto de RabbitMQ.
     * Se utiliza cuando se recibe un mensaje de RabbitMQ.
     */
    public void setRabbitTenantId(String tenantId) {
        rabbitTenantId.set(tenantId);
        log.debug("Tenant ID establecido para contexto RabbitMQ: {}", tenantId);
    }

    /**
     * Obtiene el token JWT dependiendo del contexto:
     * - Si estamos en contexto RabbitMQ (ThreadLocal), devuelve ese token
     * - Si no, utiliza el contexto HTTP normal (SecurityContext)
     */
    public String getToken() {
        String token = rabbitJwtToken.get();
        if (token != null) {
            log.debug("Usando token JWT del contexto RabbitMQ");
            return token;
        }

        try {
            token = jwtUtils.getToken();
            log.debug("Usando token JWT del contexto HTTP");
            return token;
        } catch (Exception e) {
            log.warn("No se pudo obtener token del contexto HTTP: {}", e.getMessage());
            throw new IllegalStateException("No hay token JWT disponible ni en contexto RabbitMQ ni HTTP", e);
        }
    }

    /**
     * Obtiene el tenant ID dependiendo del contexto:
     * - Si estamos en contexto RabbitMQ (ThreadLocal), devuelve ese tenant
     * - Si no, utiliza el contexto HTTP normal (SecurityContext)
     */
    public String getTenantId() {
        String tenantId = rabbitTenantId.get();
        if (tenantId != null) {
            log.debug("Usando tenant ID del contexto RabbitMQ: {}", tenantId);
            return tenantId;
        }

        try {
            tenantId = jwtUtils.getId();
            log.debug("Usando tenant ID del contexto HTTP: {}", tenantId);
            return tenantId;
        } catch (Exception e) {
            log.warn("No se pudo obtener tenant ID del contexto HTTP: {}", e.getMessage());
            throw new IllegalStateException("No hay tenant ID disponible ni en contexto RabbitMQ ni HTTP", e);
        }
    }

    /**
     * Limpia el contexto RabbitMQ para el hilo actual.
     * Debe llamarse en el finally del aspecto RabbitMQ.
     */
    public void clearRabbitContext() {
        rabbitJwtToken.remove();
        rabbitTenantId.remove();
        log.debug("Contexto RabbitMQ limpiado");
    }

    /**
     * Verifica si estamos en contexto RabbitMQ
     */
    public boolean isInRabbitContext() {
        return rabbitJwtToken.get() != null;
    }
}
