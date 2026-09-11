package com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.finanscore.motorscoring.application.command.ValidarPagoScoringCommand;
import com.finanscore.motorscoring.application.usecase.ValidarPagoScoringUseCase;
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
import java.time.LocalDate;

@Profile("payment-consumer")
@Component
public class PaymentValidationKafkaConsumer {
    public static final String GROUP_ID = "payment-validation-group";
    private static final Logger log = LoggerFactory.getLogger(PaymentValidationKafkaConsumer.class);
    private final KafkaEventReader reader;
    private final ValidarPagoScoringUseCase validarPago;
    private final ProcessedEventService processed;

    public PaymentValidationKafkaConsumer(KafkaEventReader reader, ValidarPagoScoringUseCase validarPago,
                                          ProcessedEventService processed) {
        this.reader = reader; this.validarPago = validarPago; this.processed = processed;
    }

    @KafkaListener(topics = KafkaTopicNames.SCORING_REQUESTED, groupId = GROUP_ID,
            concurrency = "${app.kafka.concurrency:3}")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record) {
        var event = reader.leer(record.value());
        if (processed.yaProcesado(event.eventId(), GROUP_ID)) {
            log.info("eventId={} correlationId={} key={} topic={} partition={} offset={} status=ALREADY_PROCESSED action=SKIP",
                    event.eventId(), event.correlationId(), record.key(), record.topic(), record.partition(), record.offset());
            return;
        }

        JsonNode p = event.payload();
        JsonNode pago = p.path("pago");
        long idSolicitud = p.path("solicitud").path("idSolicitud").asLong();

        var resultado = validarPago.ejecutar(new ValidarPagoScoringCommand(
                p.path("solicitudScoringId").asText(), idSolicitud,
                pago.path("banco").asText(), pago.path("numeroOperacion").asText(),
                new BigDecimal(pago.path("montoPagado").asText()), pago.path("moneda").asText(),
                LocalDate.parse(pago.path("fechaPago").asText()), event.correlationId(), event.eventId()));

        processed.registrar(event.eventId(), GROUP_ID, record.topic(), record.partition(), record.offset());
        log.info("eventId={} correlationId={} key={} topic={} partition={} offset={} paymentValidated={} status=PROCESSED",
                event.eventId(), event.correlationId(), record.key(), record.topic(), record.partition(), record.offset(), resultado.validado());
    }
}
