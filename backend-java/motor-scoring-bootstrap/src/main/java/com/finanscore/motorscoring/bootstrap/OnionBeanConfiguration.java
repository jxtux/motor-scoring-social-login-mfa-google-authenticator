package com.finanscore.motorscoring.bootstrap;

import com.finanscore.motorscoring.application.port.out.*;
import com.finanscore.motorscoring.application.service.*;
import com.finanscore.motorscoring.application.usecase.*;
import com.finanscore.motorscoring.domain.repository.*;
import com.finanscore.motorscoring.domain.service.*;
import com.finanscore.motorscoring.infrastructure.transaction.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Clock;


/**
 * Composition Root del sistema. Se conserva el nombre original para evidenciar
 * la evolución desde Onion hacia Clean/Hexagonal: los casos de uso siguen
 * recibiendo puertos del núcleo y PostgreSQL/Kafka permanecen en infraestructura.
 */
@Configuration
public class OnionBeanConfiguration {
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	CalculadorCapacidadPago capacidad() {
		return new CalculadorCapacidadPago();
	}

	@Bean
	CalculadorRelacionDeudaIngreso relacion() {
		return new CalculadorRelacionDeudaIngreso();
	}

	@Bean
	CalculadorRelacionCuotaIngreso relacionCuotaIngreso() {
		return new CalculadorRelacionCuotaIngreso();
	}

	@Bean
	EvaluadorReglasExcluyentes excluyentes() {
		return new EvaluadorReglasExcluyentes();
	}

	@Bean
	CalculadorScoring calculador(CalculadorCapacidadPago c, CalculadorRelacionDeudaIngreso r,
		    CalculadorRelacionCuotaIngreso rci,
			EvaluadorReglasExcluyentes e) {
		return new CalculadorScoring(c, r,rci, e);
	}

	@Bean(name = "crearCore")
	CrearSolicitudCreditoUseCase crearCore(SolicitanteRepository s, SolicitudCreditoRepository q, ProductoCrediticioRepository p, Clock c) {
		return new CrearSolicitudCreditoService(s, q, p, c);
	}

	@Bean(name = "evaluarCore")
	EjecutarEvaluacionScoringUseCase evaluarCore(SolicitudCreditoRepository q, SolicitanteRepository s, ProductoCrediticioRepository p, ModeloScoringRepository m, EvaluacionCrediticiaRepository e, CalculadorScoring c, Clock clock) {
		return new EjecutarEvaluacionScoringService(q, s, p, m, e, c, clock);
	}

	@Bean
	@Primary
	CrearSolicitudCreditoUseCase crear(@Qualifier("crearCore") CrearSolicitudCreditoUseCase core, PlatformTransactionManager tm) {
		return new TransactionalCrearSolicitudCreditoUseCase(core, new TransactionTemplate(tm));
	}

	@Bean
	@Primary
	EjecutarEvaluacionScoringUseCase evaluar(@Qualifier("evaluarCore") EjecutarEvaluacionScoringUseCase core, PlatformTransactionManager tm) {
		return new TransactionalEjecutarEvaluacionScoringUseCase(core, new TransactionTemplate(tm));
	}

	@Bean(name = "registrarScoringCore")
	RegistrarSolicitudScoringUseCase registrarScoringCore(
			@Qualifier("crear") CrearSolicitudCreditoUseCase crear,
			SolicitudScoringWorkflowPort workflows,
			OutboxEventPort outbox,
			Clock clock) {
		return new RegistrarSolicitudScoringService(crear, workflows, outbox, clock);
	}

	@Bean
	@Primary
	RegistrarSolicitudScoringUseCase registrarScoring(
			@Qualifier("registrarScoringCore") RegistrarSolicitudScoringUseCase core,
			PlatformTransactionManager tm) {
		return new TransactionalRegistrarSolicitudScoringUseCase(core, new TransactionTemplate(tm));
	}

	@Bean
	ValidarPagoScoringUseCase validarPagoScoring(PagoSimuladoPort pagos,
			SolicitudScoringWorkflowPort workflows, OutboxEventPort outbox, Clock clock) {
		return new ValidarPagoScoringService(pagos, workflows, outbox, clock);
	}

	@Bean
	ProcesarScoringValidadoUseCase procesarScoringValidado(
			@Qualifier("evaluar") EjecutarEvaluacionScoringUseCase evaluar,
			SolicitudCreditoRepository solicitudes,
			SolicitanteRepository solicitantes,
			ProductoCrediticioRepository productos,
			SolicitudScoringWorkflowPort workflows,
			OutboxEventPort outbox,
			CalculadorCapacidadPago capacidad,
			CalculadorRelacionDeudaIngreso relacion,
			CalculadorRelacionCuotaIngreso relacionCuotaIngreso,
			Clock clock) {
		return new ProcesarScoringValidadoService(evaluar, solicitudes, solicitantes, productos,
				workflows, outbox, capacidad, relacion, relacionCuotaIngreso, clock);
	}

	@Bean
	GenerarYEnviarInformeScoringUseCase generarYEnviarInformeScoring(
			PdfGenerator pdfGenerator,
			EmailSender emailSender,
			SolicitudScoringWorkflowPort workflows,
			OutboxEventPort outbox,
			Clock clock) {
		return new GenerarYEnviarInformeScoringService(pdfGenerator, emailSender, workflows, outbox, clock);
	}
}
