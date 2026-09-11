package com.finanscore.motorscoring.presentation.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoScoringRequest(
        @NotBlank @Size(max = 30) String banco,
        @NotBlank @Size(max = 80) String numeroOperacion,
        @NotNull @DecimalMin("0.01") BigDecimal montoPagado,
        @NotBlank String moneda,
        @NotNull @PastOrPresent LocalDate fechaPago) {
}
