# VERSIÓN CORREGIDA

Esta versión corrige la importación de URLs en Postman. Los requests GET no llevan Body por diseño; POST sí incluyen Body cuando corresponde.

# Postman — Motor Scoring Crediticio (IAM + JWT + Kafka)

## Archivos
- `Motor-Scoring-IAM-Kafka.postman_collection.json`
- `Motor-Scoring-Local-HTTPS.postman_environment.json`

## Importar
1. Abre Postman.
2. **Import** → importa los dos archivos.
3. Selecciona el environment **Motor Scoring - Local HTTPS**.
4. Verifica `baseUrl = https://localhost:8443`.

## HTTPS local
El backend usa HTTPS con certificado de desarrollo.

Recomendado: importar/confiar en la CA generada por el proyecto.

Alternativa solo para desarrollo:
**Postman → Settings → General → SSL certificate verification → OFF**.

## Flujo de autenticación local
Ejecuta en este orden:

1. `Registrar usuario`
2. Copia el código recibido por correo a `verificationCode`
3. `Verificar correo`
   - guarda automáticamente `setupToken`
4. `Configurar MFA`
   - guarda `totpSecret` y `otpAuthUri`
   - agrega la cuenta a Google Authenticator
5. Copia el TOTP actual de 6 dígitos a `totpCode`
6. `Confirmar MFA`
7. `Login usuario/contraseña`
   - guarda automáticamente `challengeToken`
8. Actualiza `totpCode` con un código actual
9. `Verificar MFA y obtener JWT`
   - guarda automáticamente `accessToken`
   - el backend envía `fs_refresh` como cookie HttpOnly
10. Ejecuta los requests de Scoring.
11. `Renovar Access JWT`
   - no tienes que copiar el refresh token
   - Postman lo envía automáticamente desde el cookie jar
12. `Logout`

## JWT
La colección usa `{{accessToken}}` automáticamente como:

`Authorization: Bearer {{accessToken}}`

Al hacer `MFA Verify`, el script también decodifica el payload Base64URL y lo deja en `jwtPayload`
solo para inspección. Esto **no descifra** el JWT; el JWT firmado sigue siendo legible.

## Social Login
Google:
`GET {{baseUrl}}/oauth2/authorization/google`

TikTok:
`GET {{baseUrl}}/api/v1/auth/social/tiktok/start`

Estos flujos deben completarse normalmente en un navegador porque Google/TikTok muestran login y consentimiento.
Los callbacks no deben invocarse manualmente en una prueba normal.

## Refresh Token
El refresh token **no está en una variable de Postman**.

Se guarda como cookie:

`fs_refresh; HttpOnly; Secure; SameSite=Strict`

Postman mantiene la cookie en su cookie jar y la manda automáticamente a:
- `/api/v1/auth/refresh`
- `/api/v1/auth/logout`

El backend guarda en PostgreSQL solamente el hash del refresh token asociado a la sesión.

## Scoring / Kafka
El request principal es:

`POST {{baseUrl}}/api/v1/scoring-requests`

Debe responder `202 Accepted`. Después el flujo continúa:
PostgreSQL/Outbox → Outbox Publisher → Kafka → Consumers.

## Nota sobre el ZIP original
Los endpoints de scoring sí existen en el ZIP original que se volvió a subir.
Los endpoints IAM de esta colección corresponden a la integración de seguridad preparada para ese proyecto.
Si ejecutas únicamente el ZIP original sin aplicar IAM, los requests `/api/v1/auth/**` devolverán 404.
