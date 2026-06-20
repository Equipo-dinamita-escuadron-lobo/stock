package com.stock.copy.infrastructure.adapters.output.persistence.jpa;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Flag de instancia que indica si una copia está en curso.
 * Usado por StockListener para suprimir mensajes AMQP durante la copia.
 * ADR-38 (AMQP suppression durante copy).
 */
@Component
public class CopyActiveFlag {

    private final AtomicBoolean active = new AtomicBoolean(false);

    public void activate() {
        active.set(true);
    }

    public void deactivate() {
        active.set(false);
    }

    public boolean isActive() {
        return active.get();
    }
}
