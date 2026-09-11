package com.finanscore.motorscoring.infrastructure.messaging.kafka.outbox;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("outbox-publisher")
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class OutboxSchedulingConfiguration {
}
