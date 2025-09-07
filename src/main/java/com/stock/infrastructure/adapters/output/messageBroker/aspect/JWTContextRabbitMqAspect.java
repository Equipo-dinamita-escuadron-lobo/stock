package com.stock.infrastructure.adapters.output.messageBroker.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stock.infrastructure.adapters.output.multitenancy.utils.TenantContext;
import com.stock.infrastructure.adapters.output.security.JwtDecoder;
import com.rabbitmq.client.LongString;

@Aspect
@Component
public class JWTContextRabbitMqAspect {

    private static final Logger logger = LoggerFactory.getLogger(JWTContextRabbitMqAspect.class);
    private static final String JWT_TOKEN_HEADER = "x-jwt-token";

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    /**
     * Este "advice" se ejecuta alrededor de cualquier método anotado con @RabbitListener.
     * Su función es extraer el token JWT de las cabeceras del mensaje, decodificarlo
     * para obtener el tenantId, establecerlo en el TenantContext, ejecutar el método 
     * listener y finalmente limpiar el contexto.
     */
    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object setTenantContext(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // Buscamos el objeto 'Message' en los argumentos del método del listener
        Message message = findMessageArgument(joinPoint.getArgs());

        if (message == null) {
            logger.warn("El listener [{}] no recibe un objeto 'Message'. No se puede establecer el contexto del tenant.", joinPoint.getSignature().getName());
            return joinPoint.proceed(); // Ejecutar sin contexto
        }

        Object tokenObject = message.getMessageProperties().getHeaders().get(JWT_TOKEN_HEADER);
        String jwtToken = extractTokenFromObject(tokenObject);

        if (jwtToken == null || jwtToken.isEmpty()) {
            logger.error("Mensaje recibido sin la cabecera '{}'. Se procesará sin contexto de tenant.", JWT_TOKEN_HEADER);
            return joinPoint.proceed(); // Ejecutar sin contexto
        }

        try {
            // 1. Decodificar el token para extraer el tenantId
            String tenantId = jwtDecoder.extractTenantId(jwtToken);
            
            if (tenantId == null || tenantId.isEmpty()) {
                logger.error("No se pudo extraer el tenant ID del token JWT. Se procesará sin contexto de tenant.");
                return joinPoint.proceed();
            }

            // 2. Establecer el contexto RabbitMQ en el servicio unificado
            jwtTokenService.setRabbitJwtToken(jwtToken);
            jwtTokenService.setRabbitTenantId(tenantId);

            // 3. Establecer el contexto del Tenant para este hilo
            TenantContext.setTenantId(tenantId);
            logger.info("Contexto de tenant '{}' establecido para el listener [{}].", tenantId, joinPoint.getSignature().getName());

            // 4. Ejecutar el método original del listener
            return joinPoint.proceed();

        } finally {
            logger.info("Limpiando el contexto del tenant.");
            TenantContext.clear();
            jwtTokenService.clearRabbitContext();
        }
    }

    /**
     * Método de utilidad para encontrar el argumento de tipo Message.
     */
    private Message findMessageArgument(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Message) {
                return (Message) arg;
            }
        }
        return null;
    }

    /**
     * Extrae el token JWT del objeto header que puede ser String o LongString.
     */
    private String extractTokenFromObject(Object tokenObject) {
        if (tokenObject instanceof LongString) {
            return tokenObject.toString();
        } else if (tokenObject instanceof String) {
            return (String) tokenObject;
        }
        return null;
    }
}