package com.finanscore.motorscoring.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagoSimulado(
        UUID pagoSimuladoId,
        String banco,
        String numeroOperacion,
        BigDecimal monto,
        String moneda,
        LocalDate fechaPago,
        String estado,
        boolean utilizado) {
}
