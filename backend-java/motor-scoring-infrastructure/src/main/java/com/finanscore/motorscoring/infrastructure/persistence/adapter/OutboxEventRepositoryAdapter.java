package com.finanscore.motorscoring.infrastructure.persistence.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finanscore.motorscoring.application.model.IntegrationEvent;
import com.finanscore.motorscoring.application.port.out.OutboxEventPort;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.KafkaTopicNames;
import com.finanscore.motorscoring.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.finanscore.motorscoring.infrastructure.persistence.springdata.OutboxEventSpringDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Profile("postgres")
@Repository
public class OutboxEventRepositoryAdapter implements OutboxEventPort {
    private final OutboxEventSpringDataRepository repo;
    private final ObjectMapper objectMapper;

    public OutboxEventRepositoryAdapter(OutboxEventSpringDataRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void guardarPendiente(IntegrationEvent event) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("eventId", event.eventId());
            envelope.put("eventType", event.eventType());
            envelope.put("eventVersion", event.eventVersion());
            envelope.put("occurredAt", event.occurredAt().toString());
            envelope.put("correlationId", event.correlationId());
            if (event.causationId() == null) envelope.putNull("causationId");
            else envelope.put("causationId", event.causationId());
            envelope.put("source", event.source());
            envelope.set("payload", objectMapper.valueToTree(event.payload()));

            repo.save(OutboxEventJpaEntity.pendiente(
                    UUID.randomUUID().toString(), event.eventId(), event.eventType(), event.aggregateId(),
                    KafkaTopicNames.fromEventType(event.eventType()), event.eventKey(),
                    objectMapper.writeValueAsString(envelope), event.occurredAt()));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar el evento de Outbox", e);
        }
    }
}
