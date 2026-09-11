package com.finanscore.motorscoring.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pago_simulado", uniqueConstraints = @UniqueConstraint(name = "uk_pago_simulado_operacion", columnNames = {"banco", "numero_operacion"}))
public class PagoSimuladoJpaEntity {
    @Id
    @Column(name = "pago_simulado_id")
    private UUID pagoSimuladoId;
    @Column(nullable = false, length = 30)
    private String banco;
    @Column(name = "numero_operacion", nullable = false, length = 80)
    private String numeroOperacion;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;
    @Column(nullable = false, length = 10)
    private String moneda;
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;
    @Column(nullable = false, length = 20)
    private String estado;
    @Column(nullable = false)
    private boolean utilizado;
    @Column(name = "solicitud_scoring_id", length = 36)
    private String solicitudScoringId;

    protected PagoSimuladoJpaEntity() {}
    public UUID getPagoSimuladoId() { return pagoSimuladoId; }
    public String getBanco() { return banco; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public LocalDate getFechaPago() { return fechaPago; }
    public String getEstado() { return estado; }
    public boolean isUtilizado() { return utilizado; }
    public void marcarUtilizado(String solicitudScoringId) { this.utilizado = true; this.solicitudScoringId = solicitudScoringId; }
}
