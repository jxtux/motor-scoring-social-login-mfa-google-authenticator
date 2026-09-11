package com.finanscore.motorscoring.infrastructure.messaging.kafka.dlt;

import com.finanscore.motorscoring.infrastructure.messaging.kafka.consumer.KafkaEventReader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Profile("dlt-reprocessor")
@Component
public class DltReprocessorRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DltReprocessorRunner.class);
    private final ConsumerFactory<String, String> consumerFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaEventReader reader;
    private final ConfigurableApplicationContext context;
    private final String dltTopic;
    private final String eventId;

    public DltReprocessorRunner(ConsumerFactory<String, String> consumerFactory,
                                KafkaTemplate<String, String> kafkaTemplate,
                                KafkaEventReader reader,
                                ConfigurableApplicationContext context,
                                @Value("${demo.dlt.topic:}") String dltTopic,
                                @Value("${demo.dlt.event-id:}") String eventId) {
        this.consumerFactory = consumerFactory; this.kafkaTemplate = kafkaTemplate; this.reader = reader;
        this.context = context; this.dltTopic = dltTopic; this.eventId = eventId;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (dltTopic.isBlank() || eventId.isBlank()) {
            log.error("DLT_REPROCESS_ABORTED: defina DEMO_DLT_TOPIC y DEMO_DLT_EVENT_ID");
            context.close(); return;
        }
        String originalTopic = dltTopic.endsWith(".DLT") ? dltTopic.substring(0, dltTopic.length() - 4) : dltTopic;
        try (Consumer<String, String> consumer = consumerFactory.createConsumer("dlt-reprocessor-" + UUID.randomUUID(), "demo")) {
            var partitions = consumer.partitionsFor(dltTopic);
            var assignments = new ArrayList<TopicPartition>();
            partitions.forEach(p -> assignments.add(new TopicPartition(dltTopic, p.partition())));
            consumer.assign(assignments); consumer.seekToBeginning(assignments);

            long deadline = System.currentTimeMillis() + 15000;
            ConsumerRecord<String, String> found = null;
            while (System.currentTimeMillis() < deadline && found == null) {
                for (var record : consumer.poll(Duration.ofSeconds(1))) {
                    if (eventId.equals(reader.leer(record.value()).eventId())) { found = record; break; }
                }
            }
            if (found == null) {
                log.error("DLT_EVENT_NOT_FOUND topic={} eventId={}", dltTopic, eventId);
            } else {
                var result = kafkaTemplate.send(originalTopic, found.key(), found.value()).get(15, TimeUnit.SECONDS);
                log.info("DLT_REPROCESSED eventId={} from={} to={} key={} partition={} offset={}",
                        eventId, dltTopic, originalTopic, found.key(),
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        } finally {
            context.close();
        }
    }
}
