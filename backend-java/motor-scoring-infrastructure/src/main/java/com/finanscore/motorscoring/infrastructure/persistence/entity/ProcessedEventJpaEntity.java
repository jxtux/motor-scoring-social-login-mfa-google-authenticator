package com.finanscore.motorscoring.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_event", uniqueConstraints = @UniqueConstraint(name = "uk_processed_event_consumer", columnNames = {"event_id", "consumer_name"}))
public class ProcessedEventJpaEntity {
    @Id
    @Column(name = "processed_event_id")
    private UUID processedEventId;
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;
    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;
    @Column(nullable = false, length = 120)
    private String topic;
    @Column(name = "kafka_partition", nullable = false)
    private int partition;
    @Column(name = "kafka_offset", nullable = false)
    private long offset;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEventJpaEntity() {}
    public static ProcessedEventJpaEntity crear(String eventId, String consumerName, String topic, int partition, long offset, Instant processedAt) {
        var e = new ProcessedEventJpaEntity();
        e.processedEventId = UUID.randomUUID(); e.eventId = eventId; e.consumerName = consumerName;
        e.topic = topic; e.partition = partition; e.offset = offset; e.processedAt = processedAt;
        return e;
    }
}
