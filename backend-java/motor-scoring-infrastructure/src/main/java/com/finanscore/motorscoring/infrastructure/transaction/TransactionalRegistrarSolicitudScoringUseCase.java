package com.finanscore.motorscoring.infrastructure.transaction;

import com.finanscore.motorscoring.application.command.RegistrarSolicitudScoringCommand;
import com.finanscore.motorscoring.application.dto.SolicitudScoringAceptadaDto;
import com.finanscore.motorscoring.application.usecase.RegistrarSolicitudScoringUseCase;
import org.springframework.transaction.support.TransactionTemplate;

public final class TransactionalRegistrarSolicitudScoringUseCase implements RegistrarSolicitudScoringUseCase {
    private final RegistrarSolicitudScoringUseCase delegate;
    private final TransactionTemplate transactionTemplate;

    public TransactionalRegistrarSolicitudScoringUseCase(RegistrarSolicitudScoringUseCase delegate,
                                                         TransactionTemplate transactionTemplate) {
        this.delegate = delegate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public SolicitudScoringAceptadaDto ejecutar(RegistrarSolicitudScoringCommand command) {
        return transactionTemplate.execute(status -> delegate.ejecutar(command));
    }
}
