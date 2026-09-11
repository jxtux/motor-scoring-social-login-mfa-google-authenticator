package com.finanscore.motorscoring.bootstrap.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import java.time.*;

@TestConfiguration
public class FixedTestClockConfiguration {
    @Bean
    @Primary
    Clock fixedTestClock() {
        return Clock.fixed(Instant.parse("2026-07-27T15:00:00Z"), ZoneOffset.UTC);
    }
}
