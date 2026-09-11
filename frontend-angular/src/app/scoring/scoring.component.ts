import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';
import { CurrentUserProfile } from '../auth/models/auth.models';
import { API_BASE_URL } from '../config/api.config';
import { backendErrorMessage, buildScoringValidationMessage } from './scoring-form-error.util';

interface AcceptedResponse {
  solicitudScoringId: string;
  correlationId: string;
  estado: string;
  correoElectronico: string;
  banco: string;
  numeroOperacion: string;
  fechaRegistro: string;
  mensaje: string;
}

@Component({
  selector: 'app-scoring',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './scoring.component.html'
})
export class ScoringComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly apiUrl = `${API_BASE_URL}/api/v1/scoring-requests`;
  enviando = false;
  logoutLoading = false;
  error = '';
  confirmacion?: AcceptedResponse;
  currentUser?: CurrentUserProfile;

  readonly form = this.fb.nonNullable.group({
    tipoDocumento: ['DNI', Validators.required],
    numeroDocumento: ['12345678', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombresRazonSocial: ['Cliente Demo Kafka', Validators.required],
    correoElectronico: ['', [Validators.required, Validators.email]],
    ingresosMensuales: [5500, [Validators.required, Validators.min(0.01)]],
    gastosMensuales: [1800, [Validators.required, Validators.min(0)]],
    obligacionesFinancieras: [700, [Validators.required, Validators.min(0)]],
    antiguedadLaboralNegocio: [36, [Validators.required, Validators.min(0)]],
    numeroObligacionesActivas: [2, [Validators.required, Validators.min(0)]],
    puntajeHistorialPagos: [85, [Validators.required, Validators.min(0), Validators.max(100)]],
    alertasMora: [0, [Validators.required, Validators.min(0)]],
    codigoProducto: ['PRESTAMO_PERSONAL', Validators.required],
    montoSolicitado: [15000, [Validators.required, Validators.min(0.01)]],
    plazoSolicitado: [24, [Validators.required, Validators.min(1)]],
    moneda: ['PEN', Validators.required],
    finalidadCredito: ['Consumo', Validators.required],
    banco: ['BCP', Validators.required],
    numeroOperacion: ['PAGO-DEMO-BCP-001', Validators.required],
    montoPagado: [30, [Validators.required, Validators.min(0.01)]],
    monedaPago: ['PEN', Validators.required],
    fechaPago: [new Date().toISOString().slice(0, 10), Validators.required]
  });

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({
      next: user => {
        this.currentUser = user;
        if (!this.form.controls.correoElectronico.value && user.email) {
          this.form.controls.correoElectronico.setValue(user.email);
        }
      },
      error: () => {
        // El interceptor ya intentará refresh ante 401. Si aun así falla,
        // regresamos al login y evitamos mostrar un panel sin identidad válida.
        this.router.navigate(['/login'], { replaceUrl: true });
      }
    });
  }

  cerrarSesion(): void {
    if (this.logoutLoading) return;
    this.logoutLoading = true;

    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/login'], { replaceUrl: true }),
      error: () => this.router.navigate(['/login'], { replaceUrl: true })
    });
  }

  enviar(): void {
    this.error = '';
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error = buildScoringValidationMessage(this.form);
      const firstInvalid = document.querySelector(
        'input.ng-invalid, select.ng-invalid, textarea.ng-invalid'
      ) as HTMLElement | null;
      firstInvalid?.focus();
      return;
    }
    const v = this.form.getRawValue();
    const body = {
      solicitante: {
        tipoDocumento: v.tipoDocumento,
        numeroDocumento: v.numeroDocumento,
        nombresRazonSocial: v.nombresRazonSocial,
        correoElectronico: v.correoElectronico,
        ingresosMensuales: v.ingresosMensuales,
        gastosMensuales: v.gastosMensuales,
        obligacionesFinancieras: v.obligacionesFinancieras,
        antiguedadLaboralNegocio: v.antiguedadLaboralNegocio,
        numeroObligacionesActivas: v.numeroObligacionesActivas,
        puntajeHistorialPagos: v.puntajeHistorialPagos,
        alertasMora: v.alertasMora
      },
      solicitud: {
        codigoProducto: v.codigoProducto,
        montoSolicitado: v.montoSolicitado,
        plazoSolicitado: v.plazoSolicitado,
        moneda: v.moneda,
        finalidadCredito: v.finalidadCredito
      },
      pago: {
        banco: v.banco,
        numeroOperacion: v.numeroOperacion,
        montoPagado: v.montoPagado,
        moneda: v.monedaPago,
        fechaPago: v.fechaPago
      }
    };
    this.enviando = true;
    this.http.post<AcceptedResponse>(this.apiUrl, body).subscribe({
      next: r => { this.confirmacion = r; this.enviando = false; },
      error: e => { this.error = backendErrorMessage(e); this.enviando = false; }
    });
  }

  nueva(): void { this.confirmacion = undefined; this.error = ''; }
}
