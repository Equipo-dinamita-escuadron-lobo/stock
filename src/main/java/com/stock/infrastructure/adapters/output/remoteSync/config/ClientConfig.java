package com.stock.infrastructure.adapters.output.remoteSync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;

import com.stock.infrastructure.adapters.output.messageBroker.aspect.JwtTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class ClientConfig {
    
    private final JwtTokenService jwtTokenService;
    
    /**
     * Método factory privado y genérico para crear cualquier cliente proxy.
     * Centraliza la lógica de construcción del WebClient y el HttpServiceProxyFactory.
     */
    private <T> T createWebClientProxy(WebClient.Builder webClientBuilder, String baseUrl, Class<T> clientInterface) {
        // Validar que baseUrl no sea null o vacío
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("BaseURL cannot be null or empty for client: " + clientInterface.getSimpleName());
        }
        
        log.info("Creating WebClient proxy for {} with baseUrl: {}", clientInterface.getSimpleName(), baseUrl);
        
        // 1. Construye una instancia de WebClient específica para este cliente
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(jwtPropagationFilter())
                .build();

        // 2. Crea el adaptador y la fábrica del proxy
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        // 3. Crea y devuelve el cliente
        return factory.createClient(clientInterface);
    }

    @Bean
    IProductClient productClient(WebClient.Builder webClientBuilder, ClientProperties properties) {
        String baseUrl = properties.getProducts().getBaseUrl();
        return createWebClientProxy(webClientBuilder, baseUrl, IProductClient.class);
    }

    /**
     * Filtro para propagar el token JWT en las peticiones HTTP.
     * Funciona tanto para contexto HTTP como para contexto RabbitMQ.
     */
    private ExchangeFilterFunction jwtPropagationFilter() {
        return (clientRequest, next) -> {
            try {
                String tokenValue = jwtTokenService.getToken();
                
                // Remover prefijo "Bearer " si ya existe en el token
                final String finalTokenValue = tokenValue.startsWith("Bearer ") 
                    ? tokenValue.substring(7) 
                    : tokenValue;
                
                ClientRequest newRequest = ClientRequest.from(clientRequest)
                        .headers(headers -> headers.setBearerAuth(finalTokenValue))
                        .build();

                return next.exchange(newRequest);
                
            } catch (Exception e) {
                log.error("Error al obtener token JWT para propagación: {}", e.getMessage());
                throw new IllegalStateException("No JWT token available for propagation", e);
            }
        };
    }
}