package com.finanscore.motorscoring.application.command;

import com.finanscore.motorscoring.application.model.InformeScoringData;

public record GenerarYEnviarInformeScoringCommand(
        InformeScoringData informe,
        String correlationId,
        String causationId) {
}
