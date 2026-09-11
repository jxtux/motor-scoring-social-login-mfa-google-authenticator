package com.finanscore.motorscoring.infrastructure.config;

import com.finanscore.motorscoring.infrastructure.persistence.entity.EvaluacionCrediticiaJpaEntity;
import com.finanscore.motorscoring.infrastructure.persistence.springdata.EvaluacionCrediticiaSpringDataRepository;
import com.finanscore.motorscoring.infrastructure.security.persistence.entity.UserAccountJpaEntity;
import com.finanscore.motorscoring.infrastructure.security.persistence.repository.UserAccountJpaRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registra explícitamente toda la persistencia JPA del módulo de infraestructura.
 *
 * La aplicación arranca desde el paquete bootstrap. Aunque el component scan general
 * incluye com.finanscore.motorscoring, @EntityScan y @EnableJpaRepositories tienen
 * sus propios límites de escaneo. Por ello se incluyen tanto los paquetes JPA del
 * scoring original como los paquetes JPA agregados por IAM.
 */
@Profile("postgres")
@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = {
        EvaluacionCrediticiaJpaEntity.class,
        UserAccountJpaEntity.class
})
@EnableJpaRepositories(basePackageClasses = {
        EvaluacionCrediticiaSpringDataRepository.class,
        UserAccountJpaRepository.class
})
public class JpaPersistenceConfiguration {
}
