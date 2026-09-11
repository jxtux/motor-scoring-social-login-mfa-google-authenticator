package com.finanscore.motorscoring.application.usecase;

import com.finanscore.motorscoring.application.command.ValidarPagoScoringCommand;
import com.finanscore.motorscoring.application.dto.ResultadoValidacionPagoDto;

public interface ValidarPagoScoringUseCase {
    ResultadoValidacionPagoDto ejecutar(ValidarPagoScoringCommand command);
}
