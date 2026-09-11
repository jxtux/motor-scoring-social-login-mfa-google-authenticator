package com.finanscore.motorscoring.application.service;

import com.finanscore.motorscoring.application.command.EjecutarEvaluacionScoringCommand;
import com.finanscore.motorscoring.application.command.ProcesarScoringValidadoCommand;
import com.finanscore.motorscoring.application.exception.RecursoNoEncontradoException;
import com.finanscore.motorscoring.application.model.InformeScoringData;
import com.finanscore.motorscoring.application.model.IntegrationEvent;
import com.finanscore.motorscoring.application.port.out.OutboxEventPort;
import com.finanscore.motorscoring.application.port.out.SolicitudScoringWorkflowPort;
import com.finanscore.motorscoring.application.usecase.EjecutarEvaluacionScoringUseCase;
import com.finanscore.motorscoring.application.usecase.ProcesarScoringValidadoUseCase;
import com.finanscore.motorscoring.domain.repository.ProductoCrediticioRepository;
import com.finanscore.motorscoring.domain.repository.SolicitanteRepository;
import com.finanscore.motorscoring.domain.repository.SolicitudCreditoRepository;
import com.finanscore.motorscoring.domain.service.CalculadorCapacidadPago;
import com.finanscore.motorscoring.domain.service.CalculadorRelacionCuotaIngreso;
import com.finanscore.motorscoring.domain.service.CalculadorRelacionDeudaIngreso;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ProcesarScoringValidadoService implements ProcesarScoringValidadoUseCase {
    private final EjecutarEvaluacionScoringUseCase evaluar;
    private final SolicitudCreditoRepository solicitudes;
    private final SolicitanteRepository solicitantes;
    private final ProductoCrediticioRepository productos;
    private final SolicitudScoringWorkflowPort workflows;
    private final OutboxEventPort outbox;
    private final CalculadorCapacidadPago capacidadPago;
    private final CalculadorRelacionDeudaIngreso relacionDeudaIngreso;
    private final CalculadorRelacionCuotaIngreso relacionCuotaIngreso;
    private final Clock clock;

    public ProcesarScoringValidadoService(EjecutarEvaluacionScoringUseCase evaluar,
                                          SolicitudCreditoRepository solicitudes,
                                          SolicitanteRepository solicitantes,
                                          ProductoCrediticioRepository productos,
                                          SolicitudScoringWorkflowPort workflows,
                                          OutboxEventPort outbox,
                                          CalculadorCapacidadPago capacidadPago,
                                          CalculadorRelacionDeudaIngreso relacionDeudaIngreso,
                                          CalculadorRelacionCuotaIngreso relacionCuotaIngreso,
                                          Clock clock) {
        this.evaluar = evaluar;
        this.solicitudes = solicitudes;
        this.solicitantes = solicitantes;
        this.productos = productos;
        this.workflows = workflows;
        this.outbox = outbox;
        this.capacidadPago = capacidadPago;
        this.relacionDeudaIngreso = relacionDeudaIngreso;
        this.relacionCuotaIngreso = relacionCuotaIngreso;
        this.clock = clock;
    }

    @Override
    public void ejecutar(ProcesarScoringValidadoCommand c) {
        var evaluacion = evaluar.ejecutar(new EjecutarEvaluacionScoringCommand(c.idSolicitud()));
        var solicitud = solicitudes.buscarPorId(c.idSolicitud())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + c.idSolicitud()));
        var solicitante = solicitantes.buscarPorId(solicitud.idSolicitante())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitante no encontrado."));
        var producto = productos.buscarPorId(solicitud.idProducto())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
        var workflow = workflows.buscarPorSolicitudScoringId(c.solicitudScoringId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proceso de scoring no encontrado."));

        var capacidad = capacidadPago.calcular(solicitante);
        var rdi = relacionDeudaIngreso.calcular(solicitante);
        var rci = relacionCuotaIngreso.calcular(solicitud, solicitante);

        List<InformeScoringData.FactorInformeData> factores = evaluacion.factores().stream()
                .map(f -> new InformeScoringData.FactorInformeData(
                        f.factor(), f.valorEvaluado(), f.pesoAplicado(), f.puntajeBase(),
                        f.puntajeObtenido(), f.reglaAplicada(), f.observacion(), f.excluyente()))
                .toList();

        InformeScoringData informe = new InformeScoringData(
                c.solicitudScoringId(), solicitud.id(), evaluacion.idEvaluacion(), workflow.correoElectronico(),
                solicitante.nombresRazonSocial(), solicitante.documento().tipo().name(),
                enmascararDocumento(solicitante.documento().numero()), producto.codigo(), producto.nombre(),
                solicitud.montoSolicitado().monto(), solicitud.plazoSolicitado(), solicitud.montoSolicitado().moneda().name(),
                evaluacion.puntajeTotal(), evaluacion.resultado(), evaluacion.estado(),
                solicitante.ingresosMensuales().monto(), solicitante.gastosMensuales().monto(),
                solicitante.obligacionesFinancieras().monto(), capacidad.disponible().monto(),
                rdi.porcentaje(), rci.porcentaje(), evaluacion.versionModelo(), evaluacion.fechaEvaluacion(), factores);

        workflows.actualizarEstado(c.solicitudScoringId(), "SCORING_CALCULADO");
        outbox.guardarPendiente(new IntegrationEvent(
                UUID.randomUUID().toString(), "ScoringCalculated", 1, Instant.now(clock),
                c.correlationId(), c.causationId(), "scoring-calculation-consumer",
                c.solicitudScoringId(), c.solicitudScoringId(), informePayload(informe)));
    }

    private String enmascararDocumento(String documento) {
        if (documento.length() <= 2) return "**";
        return "*".repeat(documento.length() - 2) + documento.substring(documento.length() - 2);
    }

    private Map<String, Object> informePayload(InformeScoringData i) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("solicitudScoringId", i.solicitudScoringId());
        payload.put("idSolicitud", i.idSolicitud());
        payload.put("idEvaluacion", i.idEvaluacion());
        payload.put("correoElectronico", i.correoElectronico());
        payload.put("nombreSolicitante", i.nombreSolicitante());
        payload.put("tipoDocumento", i.tipoDocumento());
        payload.put("documentoEnmascarado", i.documentoEnmascarado());
        payload.put("codigoProducto", i.codigoProducto());
        payload.put("nombreProducto", i.nombreProducto());
        payload.put("montoSolicitado", i.montoSolicitado());
        payload.put("plazoSolicitado", i.plazoSolicitado());
        payload.put("moneda", i.moneda());
        payload.put("puntajeTotal", i.puntajeTotal());
        payload.put("resultado", i.resultado());
        payload.put("estadoEvaluacion", i.estadoEvaluacion());
        payload.put("ingresosMensuales", i.ingresosMensuales());
        payload.put("gastosMensuales", i.gastosMensuales());
        payload.put("obligacionesFinancieras", i.obligacionesFinancieras());
        payload.put("capacidadPago", i.capacidadPago());
        payload.put("relacionDeudaIngreso", i.relacionDeudaIngreso());
        payload.put("relacionCuotaIngreso", i.relacionCuotaIngreso());
        payload.put("versionModelo", i.versionModelo());
        payload.put("fechaEvaluacion", i.fechaEvaluacion().toString());
        List<Map<String, Object>> factores = new ArrayList<>();
        for (var f : i.factores()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codigo", f.codigo());
            item.put("valorEvaluado", f.valorEvaluado());
            item.put("pesoAplicado", f.pesoAplicado());
            item.put("puntajeBase", f.puntajeBase());
            item.put("puntajeObtenido", f.puntajeObtenido());
            item.put("reglaAplicada", f.reglaAplicada());
            item.put("observacion", f.observacion());
            item.put("excluyente", f.excluyente());
            factores.add(item);
        }
        payload.put("factores", factores);
        return payload;
    }
}
