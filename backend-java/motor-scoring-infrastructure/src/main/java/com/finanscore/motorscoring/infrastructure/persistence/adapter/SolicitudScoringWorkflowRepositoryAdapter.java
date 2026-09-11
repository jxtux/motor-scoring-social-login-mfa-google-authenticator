package com.finanscore.motorscoring.infrastructure.persistence.adapter;

import com.finanscore.motorscoring.application.model.SolicitudScoringWorkflow;
import com.finanscore.motorscoring.application.port.out.SolicitudScoringWorkflowPort;
import com.finanscore.motorscoring.infrastructure.persistence.entity.SolicitudScoringWorkflowJpaEntity;
import com.finanscore.motorscoring.infrastructure.persistence.springdata.SolicitudScoringWorkflowSpringDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Profile("postgres")
@Repository
public class SolicitudScoringWorkflowRepositoryAdapter implements SolicitudScoringWorkflowPort {
    private final SolicitudScoringWorkflowSpringDataRepository repo;

    public SolicitudScoringWorkflowRepositoryAdapter(SolicitudScoringWorkflowSpringDataRepository repo) {
        this.repo = repo;
    }

    @Override
    public SolicitudScoringWorkflow guardar(SolicitudScoringWorkflow w) {
        var e = SolicitudScoringWorkflowJpaEntity.crear(w.solicitudScoringId(), w.usuarioAppId(), w.idSolicitud(), w.idSolicitante(),
                w.correlationId(), w.correoElectronico(), w.banco(), w.numeroOperacion(), w.montoPagado(),
                w.monedaPago(), w.fechaPago(), w.estadoProceso(), w.fechaRegistro());
        return toModel(repo.save(e));
    }

    @Override
    public Optional<SolicitudScoringWorkflow> buscarPorSolicitudScoringId(String solicitudScoringId) {
        return repo.findById(solicitudScoringId).map(this::toModel);
    }

    @Override
    public Optional<SolicitudScoringWorkflow> buscarPorIdSolicitud(Long idSolicitud) {
        return repo.findByIdSolicitud(idSolicitud).map(this::toModel);
    }

    @Override
    public void actualizarEstado(String solicitudScoringId, String estadoProceso) {
        var e = repo.findById(solicitudScoringId).orElseThrow();
        e.setEstadoProceso(estadoProceso);
        repo.save(e);
    }

    private SolicitudScoringWorkflow toModel(SolicitudScoringWorkflowJpaEntity e) {
        return new SolicitudScoringWorkflow(e.getSolicitudScoringId(), e.getUsuarioAppId(), e.getIdSolicitud(), e.getIdSolicitante(),
                e.getCorrelationId(), e.getCorreoElectronico(), e.getBanco(), e.getNumeroOperacion(),
                e.getMontoPagado(), e.getMonedaPago(), e.getFechaPago(), e.getEstadoProceso(), e.getFechaRegistro());
    }
}
