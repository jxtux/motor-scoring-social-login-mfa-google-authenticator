# Seguridad IAM integrada sobre el proyecto original

Esta versión parte del proyecto **Motor Scoring + Docker + PostgreSQL + Kafka + Outbox + Consumers + Angular** y agrega la seguridad sin reemplazar el flujo existente.

## Qué se agregó

- Registro local con correo y contraseña.
- Contraseñas con Argon2id.
- Verificación de correo con código de 6 dígitos protegido con HMAC + pepper.
- Google OAuth 2.0 + OpenID Connect.
- TikTok Login Kit Web / OAuth 2.0.
- MFA TOTP compatible con Google Authenticator.
- Secreto TOTP cifrado con AES-GCM.
- Access JWT propio firmado con RSA.
- Refresh token opaco en cookie HttpOnly; en PostgreSQL solo se guarda su SHA-256.
- RBAC con roles/permisos.
- Spring Security Resource Server antes de los controllers.
- `usuario_app_id` agregado al workflow de scoring y tomado exclusivamente de `JWT.sub`.
- Angular con login, registro, MFA, Guard, Interceptor y renovación automática del Access JWT.

`motor-scoring-domain` se conserva sin cambios.

---

## 1. Preparar `.env`

```bash
cp .env.example .env
```

Para registro local por correo y entrega de PDF configura:

```env
GMAIL_USERNAME=tu-cuenta@gmail.com
GMAIL_APP_PASSWORD=tu-app-password
IAM_MAIL_FROM=tu-cuenta@gmail.com
```

Google OAuth/OIDC:

```env
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
```

Redirect URI a registrar en Google:

```text
https://localhost:8443/login/oauth2/code/google
```

Scopes:

```text
openid profile email
```

TikTok OAuth2:

```env
TIKTOK_CLIENT_KEY=...
TIKTOK_CLIENT_SECRET=...
TIKTOK_REDIRECT_URI=https://localhost:8443/api/v1/auth/social/tiktok/callback
```

Para TikTok Login Kit Web, si el portal no acepta localhost como redirect Web, utiliza un túnel/dominio HTTPS y registra exactamente ese callback. El flujo TikTok debe iniciarse desde el mismo origen público que recibirá el callback para que la cookie `state` sea válida.

---

## 2. Generar TLS + secretos IAM locales

```bash
bash ./scripts/generate-dev-certs.sh
```

Además de los certificados originales, ahora genera:

```text
backend-java/docker/certs/iam-private.pem
backend-java/docker/certs/iam-public.pem
backend-java/docker/certs/iam-totp-aes.key
backend-java/docker/certs/iam-verification-pepper.txt
```

Son secretos **solo para desarrollo** y están ignorados por Git.

- `iam-private.pem`: firma los JWT.
- `iam-public.pem`: Spring Security valida los JWT.
- `iam-totp-aes.key`: cifra/descifra el secreto TOTP.
- `iam-verification-pepper.txt`: protege los códigos de verificación de correo.

---

## 3. Levantar el laboratorio completo

Para empezar completamente limpio:

```bash
docker compose down -v --remove-orphans
bash ./scripts/generate-dev-certs.sh
docker compose up -d --build
```

Ver estado:

```bash
docker compose ps
```

Logs API:

```bash
docker compose logs -f motor-scoring-api
```

Servicios principales:

```text
Angular       http://localhost:4200
API           https://localhost:8443
Swagger       https://localhost:8443/swagger-ui.html
Kafka UI      http://localhost:8085
PostgreSQL    localhost:5432
```

La primera ejecución limpia aplica Flyway `V1 ... V8`. V6/V7 crean IAM/RBAC y V8 agrega `usuario_app_id` al workflow de scoring.

---

## 4. Flujo local

```text
Angular
  │
  ├── Usuario + contraseña
  │       └── Verificación email
  │
  ├── Google OAuth2/OIDC
  │
  └── TikTok OAuth2
          │
          ▼
      MFA TOTP
          │
          ▼
   Access JWT + Refresh cookie HttpOnly
          │
          ▼
 Spring Security
          │ valida firma RSA / exp / issuer / audience / mfa / permissions
          ▼
 ScoringRequestController
          │ JWT.sub -> usuario_app_id
          ▼
 Application -> PostgreSQL/Outbox -> Kafka -> Consumers
```

---

## 5. Cookie de refresh en desarrollo

Angular está en `http://localhost:4200` y API en `https://localhost:8443`. La demo configura:

```env
IAM_REFRESH_COOKIE_SECURE=true
IAM_REFRESH_COOKIE_SAME_SITE=None
```

y CORS permite credenciales desde `http://localhost:4200`.

Algunos navegadores pueden aplicar políticas adicionales de bloqueo de cookies cross-site. Para un entorno equivalente a producción se recomienda servir también el frontend por HTTPS y bajo el mismo sitio/dominio.

---

## 6. Postman

La colección IAM está en:

```text
backend-java/postman/iam-security/
```

Usa el environment `Motor-Scoring-Local-HTTPS`.

---

## 7. Qué NO cambió

- Fórmulas y reglas de scoring.
- Entidades/value objects de `motor-scoring-domain`.
- Transactional Outbox.
- Topics/consumers Kafka existentes.
- TLS de Kafka/PostgreSQL.
- Generación PDF y entrega asíncrona.
