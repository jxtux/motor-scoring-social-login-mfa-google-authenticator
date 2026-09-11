package com.finanscore.motorscoring.infrastructure.persistence.springdata;

import com.finanscore.motorscoring.infrastructure.persistence.entity.PagoSimuladoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface PagoSimuladoSpringDataRepository extends JpaRepository<PagoSimuladoJpaEntity, UUID> {
    Optional<PagoSimuladoJpaEntity> findByBancoIgnoreCaseAndNumeroOperacion(String banco, String numeroOperacion);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PagoSimuladoJpaEntity p set p.utilizado=true, p.solicitudScoringId=:solicitudScoringId " +
           "where p.pagoSimuladoId=:pagoSimuladoId and p.utilizado=false")
    int marcarUtilizadoSiDisponible(@Param("pagoSimuladoId") UUID pagoSimuladoId,
                                    @Param("solicitudScoringId") String solicitudScoringId);
}
