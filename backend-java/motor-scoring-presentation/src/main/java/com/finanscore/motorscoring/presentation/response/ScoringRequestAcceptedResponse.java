package com.finanscore.motorscoring.presentation.response;

import java.time.LocalDateTime;

public record ScoringRequestAcceptedResponse(
        String solicitudScoringId,
        String correlationId,
        String estado,
        String correoElectronico,
        String banco,
        String numeroOperacion,
        LocalDateTime fechaRegistro,
        String mensaje) {
}
