package com.finanscore.motorscoring.infrastructure.persistence.springdata;

import com.finanscore.motorscoring.infrastructure.persistence.entity.SolicitudScoringWorkflowJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SolicitudScoringWorkflowSpringDataRepository extends JpaRepository<SolicitudScoringWorkflowJpaEntity, String> {
    Optional<SolicitudScoringWorkflowJpaEntity> findByIdSolicitud(Long idSolicitud);
}
