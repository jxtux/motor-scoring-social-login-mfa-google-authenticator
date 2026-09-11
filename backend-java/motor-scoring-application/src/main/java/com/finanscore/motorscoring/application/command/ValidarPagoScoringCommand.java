package com.finanscore.motorscoring.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ValidarPagoScoringCommand(
        String solicitudScoringId,
        Long idSolicitud,
        String banco,
        String numeroOperacion,
        BigDecimal montoPagado,
        String monedaPago,
        LocalDate fechaPago,
        String correlationId,
        String causationId) {
}
