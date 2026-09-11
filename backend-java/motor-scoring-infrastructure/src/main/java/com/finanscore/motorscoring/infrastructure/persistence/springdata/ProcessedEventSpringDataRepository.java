package com.finanscore.motorscoring.infrastructure.persistence.springdata;

import com.finanscore.motorscoring.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessedEventSpringDataRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
    boolean existsByEventIdAndConsumerName(String eventId, String consumerName);
}
