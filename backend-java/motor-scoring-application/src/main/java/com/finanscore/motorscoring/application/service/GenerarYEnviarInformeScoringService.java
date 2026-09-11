package com.finanscore.motorscoring.application.service;

import com.finanscore.motorscoring.application.command.GenerarYEnviarInformeScoringCommand;
import com.finanscore.motorscoring.application.model.IntegrationEvent;
import com.finanscore.motorscoring.application.port.out.EmailSender;
import com.finanscore.motorscoring.application.port.out.OutboxEventPort;
import com.finanscore.motorscoring.application.port.out.PdfGenerator;
import com.finanscore.motorscoring.application.port.out.SolicitudScoringWorkflowPort;
import com.finanscore.motorscoring.application.usecase.GenerarYEnviarInformeScoringUseCase;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class GenerarYEnviarInformeScoringService implements GenerarYEnviarInformeScoringUseCase {
    private final PdfGenerator pdfGenerator;
    private final EmailSender emailSender;
    private final SolicitudScoringWorkflowPort workflows;
    private final OutboxEventPort outbox;
    private final Clock clock;

    public GenerarYEnviarInformeScoringService(PdfGenerator pdfGenerator,
                                               EmailSender emailSender,
                                               SolicitudScoringWorkflowPort workflows,
                                               OutboxEventPort outbox,
                                               Clock clock) {
        this.pdfGenerator = pdfGenerator;
        this.emailSender = emailSender;
        this.workflows = workflows;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    public void ejecutar(GenerarYEnviarInformeScoringCommand command) {
        var informe = command.informe();
        byte[] pdf = pdfGenerator.generar(informe);

        String asunto = "Resultado de su evaluación de score crediticio";
        String contenido = "Hola " + informe.nombreSolicitante() + ",\n\n"
                + "Su evaluación crediticia ha finalizado. Puntaje: " + informe.puntajeTotal()
                + " - Resultado: " + informe.resultado() + ".\n"
                + "Adjuntamos el informe PDF con el detalle de los factores evaluados.\n\n"
                + "Este resultado es informativo y corresponde a los datos declarados y al modelo de scoring vigente.";

        emailSender.enviarConAdjunto(informe.correoElectronico(), asunto, contenido,
                "score-crediticio-" + informe.solicitudScoringId() + ".pdf", pdf);

        workflows.actualizarEstado(informe.solicitudScoringId(), "INFORME_ENVIADO");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("solicitudScoringId", informe.solicitudScoringId());
        payload.put("idEvaluacion", informe.idEvaluacion());
        payload.put("resultado", informe.resultado());
        payload.put("puntajeTotal", informe.puntajeTotal());

        outbox.guardarPendiente(new IntegrationEvent(
                UUID.randomUUID().toString(), "CreditScoreDelivered", 1, Instant.now(clock),
                command.correlationId(), command.causationId(), "credit-score-delivery-consumer",
                informe.solicitudScoringId(), informe.solicitudScoringId(), payload));
    }
}
