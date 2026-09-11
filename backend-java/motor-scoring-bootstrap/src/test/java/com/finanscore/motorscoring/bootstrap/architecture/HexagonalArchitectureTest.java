package com.finanscore.motorscoring.bootstrap.architecture;

import com.finanscore.motorscoring.domain.repository.*;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class HexagonalArchitectureTest {
    private static final String ROOT = "com.finanscore.motorscoring";

    @Test
    void domainNoDependeDeSpringKafkaJpaNiInfraestructura() {
        var classes = new ClassFileImporter().importPackages(ROOT);
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "org.apache.kafka..",
                        "jakarta.persistence..", "org.hibernate..",
                        "..infrastructure..", "..presentation..", "..bootstrap..")
                .check(classes);
    }

    @Test
    void applicationNoDependeDeInfrastructureNiPresentation() {
        var classes = new ClassFileImporter().importPackages(ROOT);
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..presentation..", "..bootstrap..")
                .check(classes);
    }

    @Test
    void adaptadoresPostgresImplementanPuertosDelNucleo() {
        var classes = new ClassFileImporter().importPackages(ROOT);
        classes().that().haveSimpleName("SolicitanteRepositoryAdapter").should().implement(SolicitanteRepository.class).check(classes);
        classes().that().haveSimpleName("SolicitudCreditoRepositoryAdapter").should().implement(SolicitudCreditoRepository.class).check(classes);
        classes().that().haveSimpleName("ProductoCrediticioRepositoryAdapter").should().implement(ProductoCrediticioRepository.class).check(classes);
        classes().that().haveSimpleName("ModeloScoringRepositoryAdapter").should().implement(ModeloScoringRepository.class).check(classes);
        classes().that().haveSimpleName("EvaluacionCrediticiaRepositoryAdapter").should().implement(EvaluacionCrediticiaRepository.class).check(classes);
    }

    @Test
    void presentationConsumePuertosDeEntradaYNoPersistenciaKafka() {
        var classes = new ClassFileImporter().importPackages(ROOT);
        classes().that().resideInAPackage("..presentation.controller..")
                .should().dependOnClassesThat().resideInAPackage("..application.usecase..")
                .check(classes);
        noClasses().that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.persistence..", "org.springframework.kafka..", "org.apache.kafka..")
                .check(classes);
    }

    @Test
    void bootstrapPuedeConocerTodosLosModulosDelProyecto() {
        var classes = new ClassFileImporter().importPackages(ROOT);
        classes().that().resideInAPackage("..bootstrap..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "org.springframework..", "org.junit..", "org.testcontainers..",
                        "com.tngtech.archunit..", ROOT + "..")
                .check(classes);
    }
}
