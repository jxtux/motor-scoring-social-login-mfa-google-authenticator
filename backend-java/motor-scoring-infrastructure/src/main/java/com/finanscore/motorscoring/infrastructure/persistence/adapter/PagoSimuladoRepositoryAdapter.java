package com.finanscore.motorscoring.infrastructure.persistence.adapter;

import com.finanscore.motorscoring.application.model.PagoSimulado;
import com.finanscore.motorscoring.application.port.out.PagoSimuladoPort;
import com.finanscore.motorscoring.infrastructure.persistence.springdata.PagoSimuladoSpringDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Profile("postgres")
@Repository
public class PagoSimuladoRepositoryAdapter implements PagoSimuladoPort {
    private final PagoSimuladoSpringDataRepository repo;

    public PagoSimuladoRepositoryAdapter(PagoSimuladoSpringDataRepository repo) { this.repo = repo; }

    @Override
    public Optional<PagoSimulado> buscar(String banco, String numeroOperacion) {
        return repo.findByBancoIgnoreCaseAndNumeroOperacion(banco, numeroOperacion)
                .map(e -> new PagoSimulado(e.getPagoSimuladoId(), e.getBanco(), e.getNumeroOperacion(),
                        e.getMonto(), e.getMoneda(), e.getFechaPago(), e.getEstado(), e.isUtilizado()));
    }

    @Override
    public boolean marcarUtilizadoSiDisponible(UUID pagoSimuladoId, String solicitudScoringId) {
        return repo.marcarUtilizadoSiDisponible(pagoSimuladoId, solicitudScoringId) == 1;
    }
}
