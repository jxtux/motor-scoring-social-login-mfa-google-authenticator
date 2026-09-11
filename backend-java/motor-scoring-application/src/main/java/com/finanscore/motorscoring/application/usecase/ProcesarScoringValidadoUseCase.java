package com.finanscore.motorscoring.application.usecase;

import com.finanscore.motorscoring.application.command.ProcesarScoringValidadoCommand;

public interface ProcesarScoringValidadoUseCase {
    void ejecutar(ProcesarScoringValidadoCommand command);
}
