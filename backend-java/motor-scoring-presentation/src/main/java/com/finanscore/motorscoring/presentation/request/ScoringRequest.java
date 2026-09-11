package com.finanscore.motorscoring.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ScoringRequest(
        @NotNull @Valid SolicitanteScoringRequest solicitante,
        @NotNull @Valid SolicitudScoringRequest solicitud,
        @NotNull @Valid PagoScoringRequest pago) {
}
