package com.finanscore.motorscoring.application.usecase;

import com.finanscore.motorscoring.application.command.GenerarYEnviarInformeScoringCommand;

public interface GenerarYEnviarInformeScoringUseCase {
    void ejecutar(GenerarYEnviarInformeScoringCommand command);
}
