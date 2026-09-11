package com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer;

import com.finanscore.motorscoring.application.command.ProcesarScoringValidadoCommand;
import com.finanscore.motorscoring.application.usecase.ProcesarScoringValidadoUseCase;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.KafkaTopicNames;
import com.finanscore.motorscoring.infrastructure.persistence.ProcessedEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("scoring-consumer")
@Component
public class ScoringCalculationKafkaConsumer {
    public static final String GROUP_ID = "scoring-calculation-group";
    private static final Logger log = LoggerFactory.getLogger(ScoringCalculationKafkaConsumer.class);
    private final KafkaEventReader reader;
    private final ProcesarScoringValidadoUseCase procesar;
    private final ProcessedEventService processed;

    public ScoringCalculationKafkaConsumer(KafkaEventReader reader, ProcesarScoringValidadoUseCase procesar,
                                           ProcessedEventService processed) {
        this.reader = reader; this.procesar = procesar; this.processed = processed;
    }

    @KafkaListener(topics = KafkaTopicNames.PAYMENT_VALIDATED, groupId = GROUP_ID,
            concurrency = "${app.kafka.concurrency:3}")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record) {
        var event = reader.leer(record.value());
        if (processed.yaProcesado(event.eventId(), GROUP_ID)) {
            log.info("eventId={} correlationId={} key={} topic={} partition={} offset={} status=ALREADY_PROCESSED action=SKIP",
                    event.eventId(), event.correlationId(), record.key(), record.topic(), record.partition(), record.offset());
            return;
        }

        var p = event.payload();
        procesar.ejecutar(new ProcesarScoringValidadoCommand(
                p.path("solicitudScoringId").asText(), p.path("idSolicitud").asLong(),
                event.correlationId(), event.eventId()));

        processed.registrar(event.eventId(), GROUP_ID, record.topic(), record.partition(), record.offset());
        log.info("eventId={} correlationId={} key={} topic={} partition={} offset={} status=PROCESSED",
                event.eventId(), event.correlationId(), record.key(), record.topic(), record.partition(), record.offset());
    }
}
