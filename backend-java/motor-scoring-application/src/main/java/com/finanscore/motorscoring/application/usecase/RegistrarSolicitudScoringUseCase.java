package com.finanscore.motorscoring.application.usecase;

import com.finanscore.motorscoring.application.command.RegistrarSolicitudScoringCommand;
import com.finanscore.motorscoring.application.dto.SolicitudScoringAceptadaDto;

public interface RegistrarSolicitudScoringUseCase {
    SolicitudScoringAceptadaDto ejecutar(RegistrarSolicitudScoringCommand command);
}
