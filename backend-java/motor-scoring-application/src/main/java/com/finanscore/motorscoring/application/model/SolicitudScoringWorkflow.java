package com.finanscore.motorscoring.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Datos de proceso que no forman parte del núcleo matemático del scoring.
 */
public record SolicitudScoringWorkflow(
        String solicitudScoringId,
        Long usuarioAppId,
        Long idSolicitud,
        Long idSolicitante,
        String correlationId,
        String correoElectronico,
        String banco,
        String numeroOperacion,
        BigDecimal montoPagado,
        String monedaPago,
        LocalDate fechaPago,
        String estadoProceso,
        LocalDateTime fechaRegistro) {
}
