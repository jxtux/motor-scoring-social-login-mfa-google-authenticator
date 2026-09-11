package com.finanscore.motorscoring.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Información de negocio necesaria para construir el PDF dirigido al usuario.
 * No contiene conceptos técnicos de Kafka.
 */
public record InformeScoringData(
        String solicitudScoringId,
        Long idSolicitud,
        Long idEvaluacion,
        String correoElectronico,
        String nombreSolicitante,
        String tipoDocumento,
        String documentoEnmascarado,
        String codigoProducto,
        String nombreProducto,
        BigDecimal montoSolicitado,
        int plazoSolicitado,
        String moneda,
        int puntajeTotal,
        String resultado,
        String estadoEvaluacion,
        BigDecimal ingresosMensuales,
        BigDecimal gastosMensuales,
        BigDecimal obligacionesFinancieras,
        BigDecimal capacidadPago,
        BigDecimal relacionDeudaIngreso,
        BigDecimal relacionCuotaIngreso,
        String versionModelo,
        LocalDateTime fechaEvaluacion,
        List<FactorInformeData> factores) {

    public InformeScoringData {
        factores = List.copyOf(factores);
    }

    public record FactorInformeData(
            String codigo,
            BigDecimal valorEvaluado,
            BigDecimal pesoAplicado,
            int puntajeBase,
            int puntajeObtenido,
            String reglaAplicada,
            String observacion,
            boolean excluyente) {
    }
}
