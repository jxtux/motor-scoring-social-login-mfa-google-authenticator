package com.finanscore.motorscoring.application.command;

import com.finanscore.motorscoring.domain.enums.Moneda;
import com.finanscore.motorscoring.domain.enums.TipoDocumento;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarSolicitudScoringCommand(
        Long usuarioAppId,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String nombresRazonSocial,
        String correoElectronico,
        BigDecimal ingresosMensuales,
        BigDecimal gastosMensuales,
        BigDecimal obligacionesFinancieras,
        int antiguedadLaboralNegocio,
        int numeroObligacionesActivas,
        int puntajeHistorialPagos,
        int alertasMora,
        String codigoProducto,
        BigDecimal montoSolicitado,
        int plazoSolicitado,
        Moneda moneda,
        String finalidadCredito,
        String banco,
        String numeroOperacion,
        BigDecimal montoPagado,
        Moneda monedaPago,
        LocalDate fechaPago) {
}
