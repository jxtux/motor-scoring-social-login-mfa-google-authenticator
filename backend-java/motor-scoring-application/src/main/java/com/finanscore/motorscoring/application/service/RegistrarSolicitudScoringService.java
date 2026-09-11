package com.finanscore.motorscoring.application.service;

import com.finanscore.motorscoring.application.command.CrearSolicitudCreditoCommand;
import com.finanscore.motorscoring.application.command.RegistrarSolicitudScoringCommand;
import com.finanscore.motorscoring.application.dto.SolicitudScoringAceptadaDto;
import com.finanscore.motorscoring.application.model.IntegrationEvent;
import com.finanscore.motorscoring.application.model.SolicitudScoringWorkflow;
import com.finanscore.motorscoring.application.port.out.OutboxEventPort;
import com.finanscore.motorscoring.application.port.out.SolicitudScoringWorkflowPort;
import com.finanscore.motorscoring.application.usecase.CrearSolicitudCreditoUseCase;
import com.finanscore.motorscoring.application.usecase.RegistrarSolicitudScoringUseCase;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registra la solicitud usando el caso de uso existente y deja el evento
 * ScoringRequested en el Outbox. Kafka no forma parte de Application.
 */
public final class RegistrarSolicitudScoringService implements RegistrarSolicitudScoringUseCase {
    private final CrearSolicitudCreditoUseCase crearSolicitud;
    private final SolicitudScoringWorkflowPort workflows;
    private final OutboxEventPort outbox;
    private final Clock clock;

    public RegistrarSolicitudScoringService(CrearSolicitudCreditoUseCase crearSolicitud,
                                            SolicitudScoringWorkflowPort workflows,
                                            OutboxEventPort outbox,
                                            Clock clock) {
        this.crearSolicitud = crearSolicitud;
        this.workflows = workflows;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    public SolicitudScoringAceptadaDto ejecutar(RegistrarSolicitudScoringCommand c) {
        String solicitudScoringId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        LocalDateTime ahora = LocalDateTime.now(clock);

        var solicitud = crearSolicitud.ejecutar(new CrearSolicitudCreditoCommand(
                solicitudScoringId,
                c.tipoDocumento(), c.numeroDocumento(), c.nombresRazonSocial(),
                c.ingresosMensuales(), c.gastosMensuales(), c.obligacionesFinancieras(),
                c.antiguedadLaboralNegocio(), c.numeroObligacionesActivas(),
                c.puntajeHistorialPagos(), c.alertasMora(), c.codigoProducto(),
                c.montoSolicitado(), c.plazoSolicitado(), c.moneda(),
                c.finalidadCredito(), "ANGULAR"));

        workflows.guardar(new SolicitudScoringWorkflow(
                solicitudScoringId, c.usuarioAppId(), solicitud.idSolicitud(), solicitud.idSolicitante(), correlationId,
                c.correoElectronico(), c.banco().toUpperCase(), c.numeroOperacion(), c.montoPagado(),
                c.monedaPago().name(), c.fechaPago(), "SOLICITUD_RECIBIDA", ahora));

        Map<String, Object> solicitante = new LinkedHashMap<>();
        solicitante.put("tipoDocumento", c.tipoDocumento().name());
        solicitante.put("numeroDocumento", c.numeroDocumento());
        solicitante.put("nombresRazonSocial", c.nombresRazonSocial());
        solicitante.put("correoElectronico", c.correoElectronico());
        solicitante.put("ingresosMensuales", c.ingresosMensuales());
        solicitante.put("gastosMensuales", c.gastosMensuales());
        solicitante.put("obligacionesFinancieras", c.obligacionesFinancieras());
        solicitante.put("antiguedadLaboralNegocio", c.antiguedadLaboralNegocio());
        solicitante.put("numeroObligacionesActivas", c.numeroObligacionesActivas());
        solicitante.put("puntajeHistorialPagos", c.puntajeHistorialPagos());
        solicitante.put("alertasMora", c.alertasMora());

        Map<String, Object> solicitudPayload = new LinkedHashMap<>();
        solicitudPayload.put("idSolicitud", solicitud.idSolicitud());
        solicitudPayload.put("codigoProducto", c.codigoProducto());
        solicitudPayload.put("montoSolicitado", c.montoSolicitado());
        solicitudPayload.put("plazoSolicitado", c.plazoSolicitado());
        solicitudPayload.put("moneda", c.moneda().name());
        solicitudPayload.put("finalidadCredito", c.finalidadCredito());

        Map<String, Object> pago = new LinkedHashMap<>();
        pago.put("banco", c.banco().toUpperCase());
        pago.put("numeroOperacion", c.numeroOperacion());
        pago.put("montoPagado", c.montoPagado());
        pago.put("moneda", c.monedaPago().name());
        pago.put("fechaPago", c.fechaPago().toString());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("solicitudScoringId", solicitudScoringId);
        payload.put("actorUserId", c.usuarioAppId());
        payload.put("solicitante", solicitante);
        payload.put("solicitud", solicitudPayload);
        payload.put("pago", pago);

        outbox.guardarPendiente(new IntegrationEvent(
                UUID.randomUUID().toString(), "ScoringRequested", 1, Instant.now(clock),
                correlationId, null, "motor-scoring-api", solicitudScoringId,
                solicitudScoringId, payload));

        return new SolicitudScoringAceptadaDto(
                solicitudScoringId, correlationId, "SOLICITUD_RECIBIDA",
                c.correoElectronico(), c.banco().toUpperCase(), c.numeroOperacion(), ahora);
    }
}
