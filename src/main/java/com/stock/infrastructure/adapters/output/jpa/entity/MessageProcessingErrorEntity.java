package com.stock.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief JPA entity for tracking RabbitMQ message processing errors
 * 
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "message_processing_errors")
public class MessageProcessingErrorEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "error_description", columnDefinition = "TEXT")
    private String errorDescription;

    @Column(name = "message_data", columnDefinition = "TEXT")
    private String messageData;

    @Column(name = "error_timestamp", nullable = false)
    private Instant errorTimestamp;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    /**
     * @brief Sets error timestamp before entity persistence
     */
    @PrePersist
    protected void onCreate() {
        if (errorTimestamp == null) {
            errorTimestamp = Instant.now();
        }
    }

    @TenantId
    String tenantId;
}
