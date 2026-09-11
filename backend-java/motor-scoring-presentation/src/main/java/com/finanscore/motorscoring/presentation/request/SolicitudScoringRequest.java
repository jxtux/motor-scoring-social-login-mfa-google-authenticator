package com.finanscore.motorscoring.presentation.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record SolicitudScoringRequest(
        @NotBlank @Size(max = 30) String codigoProducto,
        @NotNull @DecimalMin("0.01") BigDecimal montoSolicitado,
        @Positive int plazoSolicitado,
        @NotBlank String moneda,
        @NotBlank @Size(max = 150) String finalidadCredito) {
}
