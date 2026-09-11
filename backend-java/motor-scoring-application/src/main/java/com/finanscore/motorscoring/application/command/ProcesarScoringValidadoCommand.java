package com.finanscore.motorscoring.application.command;

public record ProcesarScoringValidadoCommand(
        String solicitudScoringId,
        Long idSolicitud,
        String correlationId,
        String causationId) {
}
