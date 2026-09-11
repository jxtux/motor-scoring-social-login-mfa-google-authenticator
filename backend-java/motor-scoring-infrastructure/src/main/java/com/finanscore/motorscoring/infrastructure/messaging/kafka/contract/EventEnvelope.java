package com.finanscore.motorscoring.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record EventEnvelope(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String causationId,
        String source,
        JsonNode payload) {
}
