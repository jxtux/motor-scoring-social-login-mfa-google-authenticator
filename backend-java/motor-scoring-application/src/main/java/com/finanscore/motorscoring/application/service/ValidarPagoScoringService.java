package com.finanscore.motorscoring.application.service;

import com.finanscore.motorscoring.application.command.ValidarPagoScoringCommand;
import com.finanscore.motorscoring.application.dto.ResultadoValidacionPagoDto;
import com.finanscore.motorscoring.application.exception.RecursoNoEncontradoException;
import com.finanscore.motorscoring.application.model.IntegrationEvent;
import com.finanscore.motorscoring.application.model.PagoSimulado;
import com.finanscore.motorscoring.application.port.out.OutboxEventPort;
import com.finanscore.motorscoring.application.port.out.PagoSimuladoPort;
import com.finanscore.motorscoring.application.port.out.SolicitudScoringWorkflowPort;
import com.finanscore.motorscoring.application.usecase.ValidarPagoScoringUseCase;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ValidarPagoScoringService implements ValidarPagoScoringUseCase {
    private final PagoSimuladoPort pagos;
    private final SolicitudScoringWorkflowPort workflows;
    private final OutboxEventPort outbox;
    private final Clock clock;

    public ValidarPagoScoringService(PagoSimuladoPort pagos,
                                     SolicitudScoringWorkflowPort workflows,
                                     OutboxEventPort outbox,
                                     Clock clock) {
        this.pagos = pagos;
        this.workflows = workflows;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    public ResultadoValidacionPagoDto ejecutar(ValidarPagoScoringCommand c) {
        var workflow = workflows.buscarPorSolicitudScoringId(c.solicitudScoringId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proceso de scoring no encontrado: " + c.solicitudScoringId()));

        Optional<PagoSimulado> encontrado = pagos.buscar(c.banco(), c.numeroOperacion());
        String motivo = validar(encontrado, c);
        boolean valido = motivo == null;
        if (valido && !pagos.marcarUtilizadoSiDisponible(encontrado.orElseThrow().pagoSimuladoId(), c.solicitudScoringId())) {
            valido = false;
            motivo = "El pago ya fue utilizado por otra solicitud.";
        }

        String eventType;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("solicitudScoringId", c.solicitudScoringId());
        payload.put("idSolicitud", c.idSolicitud());
        payload.put("correoElectronico", workflow.correoElectronico());
        payload.put("banco", c.banco());
        payload.put("numeroOperacion", c.numeroOperacion());

        if (valido) {
            workflows.actualizarEstado(c.solicitudScoringId(), "PAGO_VALIDADO");
            eventType = "PaymentValidated";
            payload.put("estadoPago", "VALIDADO");
        } else {
            workflows.actualizarEstado(c.solicitudScoringId(), "PAGO_RECHAZADO");
            eventType = "PaymentRejected";
            payload.put("estadoPago", "RECHAZADO");
            payload.put("motivo", motivo);
        }

        outbox.guardarPendiente(new IntegrationEvent(
                UUID.randomUUID().toString(), eventType, 1, Instant.now(clock),
                c.correlationId(), c.causationId(), "payment-validation-consumer",
                c.solicitudScoringId(), c.solicitudScoringId(), payload));

        return new ResultadoValidacionPagoDto(valido, valido ? "Pago validado" : motivo);
    }

    private String validar(Optional<PagoSimulado> encontrado, ValidarPagoScoringCommand c) {
        if (encontrado.isEmpty()) return "Pago no encontrado para el banco y número de operación indicados.";
        PagoSimulado p = encontrado.get();
        if (!"CONFIRMADO".equalsIgnoreCase(p.estado())) return "El pago no se encuentra confirmado.";
        if (p.utilizado()) return "El pago ya fue utilizado por otra solicitud.";
        if (p.monto().compareTo(c.montoPagado()) != 0) return "El monto pagado no coincide.";
        if (!p.moneda().equalsIgnoreCase(c.monedaPago())) return "La moneda del pago no coincide.";
        if (!p.fechaPago().equals(c.fechaPago())) return "La fecha del pago no coincide.";
        return null;
    }
}
