package com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.finanscore.motorscoring.application.command.GenerarYEnviarInformeScoringCommand;
import com.finanscore.motorscoring.application.model.InformeScoringData;
import com.finanscore.motorscoring.application.usecase.GenerarYEnviarInformeScoringUseCase;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.KafkaTopicNames;
import com.finanscore.motorscoring.infrastructure.persistence.ProcessedEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Profile("delivery-consumer")
@Component
public class CreditScoreDeliveryKafkaConsumer {
    public static final String GROUP_ID = "credit-score-delivery-group";
    private static final Logger log = LoggerFactory.getLogger(CreditScoreDeliveryKafkaConsumer.class);
    private final KafkaEventReader reader;
    private final GenerarYEnviarInformeScoringUseCase entregar;
    private final ProcessedEventService processed;

    public CreditScoreDeliveryKafkaConsumer(KafkaEventReader reader, GenerarYEnviarInformeScoringUseCase entregar,
                                            ProcessedEventService processed) {
        this.reader = reader; this.entregar = entregar; this.processed = processed;
    }

    @KafkaListener(topics = KafkaTopicNames.SCORING_CALCULATED, groupId = GROUP_ID,
            concurrency = "${app.kafka.concurrency:3}")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record) {
        var event = reader.leer(record.value());
        if (processed.yaProcesado(event.eventId(), GROUP_ID)) {
            log.info("eventId={} correlationId={} key={} topic={} partition={} offset={} status=ALREADY_PROCESSED action=SKIP",
                    event.eventId(), event.correlationId(), record.key(), record.topic(), record.partition(), record.offset());
            return;
        }

        entregar.ejecutar(new GenerarYEnviarInformeScoringCommand(toInforme(event.payload()),
                event.correlationId(), event.eventId()));

        processed.registrar(event.eventId(), GROUP_ID, record.topic(), record.partition(), record.offset());
        log.info("eventId={} correlationId={} key={} topic={} partition={} offset={} status=PROCESSED",
                event.eventId(), event.correlationId(), record.key(), record.topic(), record.partition(), record.offset());
    }

    private InformeScoringData toInforme(JsonNode p) {
        List<InformeScoringData.FactorInformeData> factores = new ArrayList<>();
        for (JsonNode f : p.path("factores")) {
            factores.add(new InformeScoringData.FactorInformeData(
                    f.path("codigo").asText(), decimal(f, "valorEvaluado"), decimal(f, "pesoAplicado"),
                    f.path("puntajeBase").asInt(), f.path("puntajeObtenido").asInt(),
                    f.path("reglaAplicada").asText(), f.path("observacion").asText(), f.path("excluyente").asBoolean()));
        }
        return new InformeScoringData(
                p.path("solicitudScoringId").asText(), p.path("idSolicitud").asLong(), p.path("idEvaluacion").asLong(),
                p.path("correoElectronico").asText(), p.path("nombreSolicitante").asText(), p.path("tipoDocumento").asText(),
                p.path("documentoEnmascarado").asText(), p.path("codigoProducto").asText(), p.path("nombreProducto").asText(),
                decimal(p, "montoSolicitado"), p.path("plazoSolicitado").asInt(), p.path("moneda").asText(),
                p.path("puntajeTotal").asInt(), p.path("resultado").asText(), p.path("estadoEvaluacion").asText(),
                decimal(p, "ingresosMensuales"), decimal(p, "gastosMensuales"), decimal(p, "obligacionesFinancieras"),
                decimal(p, "capacidadPago"), decimal(p, "relacionDeudaIngreso"), decimal(p, "relacionCuotaIngreso"),
                p.path("versionModelo").asText(), LocalDateTime.parse(p.path("fechaEvaluacion").asText()), factores);
    }

    private BigDecimal decimal(JsonNode node, String field) {
        return new BigDecimal(node.path(field).asText());
    }
}
