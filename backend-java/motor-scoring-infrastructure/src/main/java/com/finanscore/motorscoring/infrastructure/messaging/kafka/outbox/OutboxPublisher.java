package com.finanscore.motorscoring.infrastructure.messaging.kafka.outbox;

import com.finanscore.motorscoring.infrastructure.persistence.springdata.OutboxEventSpringDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Profile("outbox-publisher")
@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventSpringDataRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventSpringDataRepository repo, KafkaTemplate<String, String> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:1000}")
    @Transactional
    public void publicarPendientes() {
        for (var event : repo.findTop50ByStatusOrderByCreatedAtAsc("PENDING")) {
            try {
                var result = kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload())
                        .get(15, TimeUnit.SECONDS);
                event.marcarPublicado(Instant.now());
                repo.save(event);
                log.info("OUTBOX_PUBLISHED eventId={} eventType={} topic={} key={} partition={} offset={}",
                        event.getEventId(), event.getEventType(), event.getTopic(), event.getEventKey(),
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } catch (Exception ex) {
                log.error("OUTBOX_PUBLISH_FAILED eventId={} topic={} cause={}",
                        event.getEventId(), event.getTopic(), ex.getMessage());
                throw new IllegalStateException("No se pudo publicar el evento Outbox " + event.getEventId(), ex);
            }
        }
    }
}
