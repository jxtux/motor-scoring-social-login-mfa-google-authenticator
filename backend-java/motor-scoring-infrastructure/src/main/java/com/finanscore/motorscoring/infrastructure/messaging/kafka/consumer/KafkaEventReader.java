package com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.contract.EventEnvelope;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class KafkaEventReader {
    private final ObjectMapper objectMapper;

    public KafkaEventReader(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public EventEnvelope leer(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return new EventEnvelope(
                    root.path("eventId").asText(), root.path("eventType").asText(),
                    root.path("eventVersion").asInt(), Instant.parse(root.path("occurredAt").asText()),
                    root.path("correlationId").asText(),
                    root.path("causationId").isNull() ? null : root.path("causationId").asText(),
                    root.path("source").asText(), root.path("payload"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Evento Kafka inválido", e);
        }
    }
}
