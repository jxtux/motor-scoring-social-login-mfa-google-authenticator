package com.finanscore.motorscoring.infrastructure.persistence.springdata;

import com.finanscore.motorscoring.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventSpringDataRepository extends JpaRepository<OutboxEventJpaEntity, String> {
    List<OutboxEventJpaEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
