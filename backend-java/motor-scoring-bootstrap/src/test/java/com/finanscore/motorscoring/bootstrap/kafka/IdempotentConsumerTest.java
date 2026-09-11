package com.finanscore.motorscoring.bootstrap.kafka;

import com.finanscore.motorscoring.application.usecase.ProcesarScoringValidadoUseCase;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer.KafkaEventReader;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer.ScoringCalculationKafkaConsumer;
import com.finanscore.motorscoring.infrastructure.messaging.kafka.contract.EventEnvelope;
import com.finanscore.motorscoring.infrastructure.persistence.ProcessedEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class IdempotentConsumerTest {
    @Test
    void debeOmitirUnEventIdYaProcesado() throws Exception {
        KafkaEventReader reader = mock(KafkaEventReader.class);
        ProcesarScoringValidadoUseCase useCase = mock(ProcesarScoringValidadoUseCase.class);
        ProcessedEventService processed = mock(ProcessedEventService.class);
        var payload = new ObjectMapper().readTree("{\"solicitudScoringId\":\"SOL-A001\",\"idSolicitud\":1}");
        var envelope = new EventEnvelope("EVENT-001", "PaymentValidated", 1, Instant.now(),
                "PROCESS-001", "EVENT-000", "payment-validation-consumer", payload);
        when(reader.leer(anyString())).thenReturn(envelope);
        when(processed.yaProcesado("EVENT-001", ScoringCalculationKafkaConsumer.GROUP_ID)).thenReturn(true);

        var consumer = new ScoringCalculationKafkaConsumer(reader, useCase, processed);
        consumer.onMessage(new ConsumerRecord<>("payment.validated.v1", 1, 12L, "SOL-A001", "{}"));

        verifyNoInteractions(useCase);
        verify(processed, never()).registrar(anyString(), anyString(), anyString(), anyInt(), anyLong());
    }
}
