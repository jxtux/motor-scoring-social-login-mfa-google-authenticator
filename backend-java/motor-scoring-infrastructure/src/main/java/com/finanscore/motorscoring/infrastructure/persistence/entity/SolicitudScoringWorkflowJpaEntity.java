package com.finanscore.motorscoring.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_scoring_workflow")
public class SolicitudScoringWorkflowJpaEntity {
    @Id
    @Column(name = "solicitud_scoring_id", length = 36)
    private String solicitudScoringId;
    @Column(name = "usuario_app_id")
    private Long usuarioAppId;
    @Column(name = "id_solicitud", nullable = false, unique = true)
    private Long idSolicitud;
    @Column(name = "id_solicitante", nullable = false)
    private Long idSolicitante;
    @Column(name = "correlation_id", nullable = false, length = 36)
    private String correlationId;
    @Column(name = "correo_electronico", nullable = false, length = 200)
    private String correoElectronico;
    @Column(nullable = false, length = 30)
    private String banco;
    @Column(name = "numero_operacion", nullable = false, length = 80)
    private String numeroOperacion;
    @Column(name = "monto_pagado", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoPagado;
    @Column(name = "moneda_pago", nullable = false, length = 10)
    private String monedaPago;
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;
    @Column(name = "estado_proceso", nullable = false, length = 40)
    private String estadoProceso;
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    protected SolicitudScoringWorkflowJpaEntity() {}

    public String getSolicitudScoringId() { return solicitudScoringId; }
    public Long getUsuarioAppId() { return usuarioAppId; }
    public Long getIdSolicitud() { return idSolicitud; }
    public Long getIdSolicitante() { return idSolicitante; }
    public String getCorrelationId() { return correlationId; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getBanco() { return banco; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public String getMonedaPago() { return monedaPago; }
    public LocalDate getFechaPago() { return fechaPago; }
    public String getEstadoProceso() { return estadoProceso; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setEstadoProceso(String estadoProceso) { this.estadoProceso = estadoProceso; }

    public static SolicitudScoringWorkflowJpaEntity crear(String solicitudScoringId, Long usuarioAppId, Long idSolicitud, Long idSolicitante,
            String correlationId, String correoElectronico, String banco, String numeroOperacion,
            BigDecimal montoPagado, String monedaPago, LocalDate fechaPago, String estadoProceso, LocalDateTime fechaRegistro) {
        var e = new SolicitudScoringWorkflowJpaEntity();
        e.solicitudScoringId = solicitudScoringId; e.usuarioAppId = usuarioAppId; e.idSolicitud = idSolicitud; e.idSolicitante = idSolicitante;
        e.correlationId = correlationId; e.correoElectronico = correoElectronico; e.banco = banco;
        e.numeroOperacion = numeroOperacion; e.montoPagado = montoPagado; e.monedaPago = monedaPago;
        e.fechaPago = fechaPago; e.estadoProceso = estadoProceso; e.fechaRegistro = fechaRegistro;
        return e;
    }
}
