package com.finanscore.motorscoring.presentation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Motor de Scoring Crediticio API")
                .version("2.0.2")
                .description("Motor de Scoring Crediticio con Clean Architecture, PostgreSQL y procesamiento asíncrono EDA mediante Apache Kafka."));
    }
}
