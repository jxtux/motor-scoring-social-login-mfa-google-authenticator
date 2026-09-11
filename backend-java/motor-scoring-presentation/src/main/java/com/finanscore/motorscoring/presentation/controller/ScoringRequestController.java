package com.finanscore.motorscoring.presentation.controller;

import com.finanscore.motorscoring.application.command.RegistrarSolicitudScoringCommand;
import com.finanscore.motorscoring.application.usecase.RegistrarSolicitudScoringUseCase;
import com.finanscore.motorscoring.domain.enums.Moneda;
import com.finanscore.motorscoring.domain.enums.TipoDocumento;
import com.finanscore.motorscoring.presentation.request.ScoringRequest;
import com.finanscore.motorscoring.presentation.response.ScoringRequestAcceptedResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scoring-requests")
public class ScoringRequestController {
    private final RegistrarSolicitudScoringUseCase registrar;

    public ScoringRequestController(RegistrarSolicitudScoringUseCase registrar) {
        this.registrar = registrar;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCORE_CREATE')")
    @Operation(summary = "Registrar solicitud asíncrona de score crediticio",
            description = "Valida el contrato REST, registra la solicitud y deja ScoringRequested en el Outbox para su publicación a Kafka.")
    public ResponseEntity<ScoringRequestAcceptedResponse> registrar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ScoringRequest request) {
        var s = request.solicitante();
        var q = request.solicitud();
        var p = request.pago();

        Long usuarioAppId = Long.valueOf(jwt.getSubject());

        var result = registrar.ejecutar(new RegistrarSolicitudScoringCommand(
                usuarioAppId,
                TipoDocumento.valueOf(s.tipoDocumento().toUpperCase()), s.numeroDocumento(), s.nombresRazonSocial(),
                s.correoElectronico(), s.ingresosMensuales(), s.gastosMensuales(), s.obligacionesFinancieras(),
                s.antiguedadLaboralNegocio(), s.numeroObligacionesActivas(), s.puntajeHistorialPagos(), s.alertasMora(),
                q.codigoProducto(), q.montoSolicitado(), q.plazoSolicitado(), Moneda.valueOf(q.moneda().toUpperCase()),
                q.finalidadCredito(), p.banco(), p.numeroOperacion(), p.montoPagado(),
                Moneda.valueOf(p.moneda().toUpperCase()), p.fechaPago()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ScoringRequestAcceptedResponse(
                result.solicitudScoringId(), result.correlationId(), result.estado(), result.correoElectronico(),
                result.banco(), result.numeroOperacion(), result.fechaRegistro(),
                "Solicitud registrada. El resultado y el informe PDF serán enviados al correo indicado."));
    }
}
