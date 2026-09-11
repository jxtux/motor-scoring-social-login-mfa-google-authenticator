package com.finanscore.motorscoring.application.dto;

import java.time.LocalDateTime;

public record SolicitudScoringAceptadaDto(
        String solicitudScoringId,
        String correlationId,
        String estado,
        String correoElectronico,
        String banco,
        String numeroOperacion,
        LocalDateTime fechaRegistro) {
}
