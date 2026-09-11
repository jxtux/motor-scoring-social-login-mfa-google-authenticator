package com.finanscore.motorscoring.infrastructure.persistence;

import com.finanscore.motorscoring.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import com.finanscore.motorscoring.infrastructure.persistence.springdata.ProcessedEventSpringDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Profile("postgres")
@Service
public class ProcessedEventService {
    private final ProcessedEventSpringDataRepository repo;

    public ProcessedEventService(ProcessedEventSpringDataRepository repo) { this.repo = repo; }

    public boolean yaProcesado(String eventId, String consumerName) {
        return repo.existsByEventIdAndConsumerName(eventId, consumerName);
    }

    public void registrar(String eventId, String consumerName, String topic, int partition, long offset) {
        repo.save(ProcessedEventJpaEntity.crear(eventId, consumerName, topic, partition, offset, Instant.now()));
    }
}
