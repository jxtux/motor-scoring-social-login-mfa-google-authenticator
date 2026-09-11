package com.finanscore.motorscoring.application.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Mensaje de integración agnóstico de Kafka. Application expresa qué evento
 * debe publicarse; Infrastructure decide cómo serializarlo y transportarlo.
 */
public record IntegrationEvent(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String causationId,
        String source,
        String aggregateId,
        String eventKey,
        Map<String, Object> payload) {

    public IntegrationEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(correlationId);
        Objects.requireNonNull(source);
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(eventKey);
        payload = Map.copyOf(payload);
    }
}
