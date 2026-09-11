package com.finanscore.motorscoring.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox_event", indexes = @Index(name = "idx_outbox_status_created", columnList = "status,created_at"))
public class OutboxEventJpaEntity {
    @Id
    @Column(name = "outbox_event_id", length = 36)
    private String outboxEventId;
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;
    @Column(nullable = false, length = 120)
    private String topic;
    @Column(name = "event_key", nullable = false, length = 100)
    private String eventKey;
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEventJpaEntity() {}

    public static OutboxEventJpaEntity pendiente(String outboxEventId, String eventId, String eventType,
                                                   String aggregateId, String topic, String eventKey,
                                                   String payload, Instant createdAt) {
        var e = new OutboxEventJpaEntity();
        e.outboxEventId = outboxEventId; e.eventId = eventId; e.eventType = eventType;
        e.aggregateId = aggregateId; e.topic = topic; e.eventKey = eventKey; e.payload = payload;
        e.status = "PENDING"; e.createdAt = createdAt;
        return e;
    }
    public String getOutboxEventId() { return outboxEventId; }
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getTopic() { return topic; }
    public String getEventKey() { return eventKey; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void marcarPublicado(Instant publishedAt) { this.status = "PUBLISHED"; this.publishedAt = publishedAt; }
}
