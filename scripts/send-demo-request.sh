#!/usr/bin/env bash
set -euo pipefail
EMAIL=${1:?Uso: $0 <correo> [dni] [operacion]}
DNI=${2:-12345678}
OPERACION=${3:-PAGO-DEMO-BCP-001}
TODAY=$(date +%F)
curl -k -i https://localhost:8443/api/v1/scoring-requests \
  -H 'Content-Type: application/json' \
  -d "{
    \"solicitante\": {
      \"tipoDocumento\": \"DNI\",
      \"numeroDocumento\": \"${DNI}\",
      \"nombresRazonSocial\": \"Persona Demo Kafka\",
      \"correoElectronico\": \"${EMAIL}\",
      \"ingresosMensuales\": 5500,
      \"gastosMensuales\": 1800,
      \"obligacionesFinancieras\": 700,
      \"antiguedadLaboralNegocio\": 36,
      \"numeroObligacionesActivas\": 2,
      \"puntajeHistorialPagos\": 85,
      \"alertasMora\": 0
    },
    \"solicitud\": {
      \"codigoProducto\": \"PRESTAMO_PERSONAL\",
      \"montoSolicitado\": 15000,
      \"plazoSolicitado\": 24,
      \"moneda\": \"PEN\",
      \"finalidadCredito\": \"Consumo\"
    },
    \"pago\": {
      \"banco\": \"BCP\",
      \"numeroOperacion\": \"${OPERACION}\",
      \"montoPagado\": 30,
      \"moneda\": \"PEN\",
      \"fechaPago\": \"${TODAY}\"
    }
  }"
