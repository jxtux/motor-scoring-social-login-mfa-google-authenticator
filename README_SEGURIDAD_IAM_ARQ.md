# Seguridad e IAM — Motor de Scoring Crediticio

> **Proyecto base:** Motor de Scoring Crediticio — Clean Architecture + Apache Kafka  
> **Estado actual:** integración de Seguridad/IAM implementada sobre el proyecto original, con autenticación local, verificación de correo, MFA TOTP, JWT propio, Social Login con Google y TikTok, gestión de sesiones, RBAC y protección del módulo de scoring.  
> **Objetivo de este documento:** registrar qué cambió respecto del proyecto original, qué componentes se agregaron y cómo quedó el flujo final de autenticación/autorización.

---

## 1. Resumen de cambios respecto del proyecto original

El proyecto original tenía como objetivo principal procesar solicitudes de scoring mediante una arquitectura limpia y orientada a eventos:

```text
Angular
   ↓
REST API
   ↓
Application
   ↓
PostgreSQL
   ↓
Transactional Outbox
   ↓
Kafka
   ↓
Payment Consumer
   ↓
Scoring Consumer
   ↓
Delivery Consumer
```

Sobre esa base se agregó una capa completa de Seguridad e IAM sin trasladar dependencias de seguridad al dominio de scoring.

La arquitectura resultante es:

```text
                   ┌─────────────────────────┐
                   │      Angular 20         │
                   │ Login / Registro / MFA  │
                   │ Scoring / Sesión        │
                   └────────────┬────────────┘
                                │
                                ▼
                   ┌─────────────────────────┐
                   │      Spring Security    │
                   │ OAuth2 / OIDC / JWT     │
                   │ MFA / RBAC / Sessions   │
                   └────────────┬────────────┘
                                │
                                ▼
                   ┌─────────────────────────┐
                   │     Application IAM     │
                   │ casos de uso + puertos  │
                   └────────────┬────────────┘
                                │
               ┌────────────────┼─────────────────┐
               ▼                ▼                 ▼
         PostgreSQL         Google OAuth       TikTok OAuth
         IAM / sesión       + OIDC             Login Kit
               │
               ▼
         Gmail SMTP
         verificación
                                │
                                ▼
                   ┌─────────────────────────┐
                   │   Módulo de Scoring     │
                   │ dominio original intacto│
                   └────────────┬────────────┘
                                │
                                ▼
                      Outbox → Kafka → Consumers
```

---

## 2. Principio arquitectónico aplicado

La seguridad fue tratada como una capacidad transversal externa al dominio.

```text
IAM / OAuth / MFA / JWT / Spring Security
                    ↓
      Presentation / Application
                    ↓
          dominio de scoring
                    ↓
      Infrastructure / Kafka / Outbox
```

### Resultado

El módulo `motor-scoring-domain` continúa sin depender de:

- Spring Security
- OAuth 2.0
- OpenID Connect
- Google
- TikTok
- JWT
- TOTP
- JPA
- Kafka
- SMTP

La lógica de scoring se mantiene independiente de los mecanismos de autenticación.

---

## 3. Cambios por módulo

### 3.1. `motor-scoring-domain`

Se mantiene framework-independent.

No se introdujeron conceptos de login, tokens, redes sociales o MFA dentro del dominio de scoring.

Esto preserva Clean Architecture y evita que reglas del negocio dependan de infraestructura o seguridad.

---

### 3.2. `motor-scoring-application`

Se incorporaron casos de uso y puertos relacionados con IAM.

Responsabilidades principales:

```text
Registro de usuario
Verificación de correo
Reenvío de código
Login local
Procesamiento de identidad social
Configuración de MFA
Confirmación de MFA
Verificación de MFA
Refresh de sesión
Logout
Obtención del usuario autenticado
Roles y permisos
```

El módulo Application sigue evitando dependencias directas de:

```text
Spring Security
JPA
OAuth SDKs
Google SDK
TikTok SDK
Nimbus concreto
SMTP concreto
```

Estas capacidades son expuestas mediante puertos y resueltas por adaptadores de infraestructura.

---

### 3.3. `motor-scoring-infrastructure`

Se incorporaron adaptadores técnicos para Seguridad/IAM:

```text
security/
├── persistence/
├── password/
├── jwt/
├── totp/
├── oauth/
│   ├── google/
│   └── tiktok/
├── email/
└── session/
```

Responsabilidades incorporadas:

- Persistencia IAM en PostgreSQL mediante JPA.
- Hash de contraseñas con Argon2id.
- Generación y validación de JWT RSA.
- Generación y validación de TOTP.
- Cifrado AES-GCM del secreto MFA.
- Integración Google OAuth2/OIDC.
- Integración TikTok OAuth2.
- Envío de códigos de verificación por Gmail SMTP.
- Persistencia y revocación de sesiones.
- Hash del refresh token.

---

### 3.4. `motor-scoring-presentation`

Se agregaron endpoints REST para autenticación y sesión.

Entre los principales:

```text
POST /api/v1/auth/register
POST /api/v1/auth/verify-email
POST /api/v1/auth/resend-email-code
POST /api/v1/auth/login

POST /api/v1/auth/mfa/setup
POST /api/v1/auth/mfa/confirm
POST /api/v1/auth/mfa/verify

POST /api/v1/auth/refresh
POST /api/v1/auth/logout

GET  /api/v1/users/me
```

También se integraron los callbacks del login social:

```text
Google OAuth2 / OIDC
/login/oauth2/code/google

TikTok OAuth2
/api/v1/auth/social/tiktok/start
/api/v1/auth/social/tiktok/callback
```

Los endpoints del scoring quedaron protegidos mediante Spring Security y JWT.

---

### 3.5. `motor-scoring-bootstrap`

Se incorporó la configuración central de seguridad:

- Spring Security.
- OAuth2 Client.
- OAuth2 Resource Server.
- JWT RSA.
- Security filter chain.
- CORS.
- configuración de cookies.
- configuración MFA.
- resolución de secretos desde archivo o variable de entorno.
- integración Gmail.
- propiedades Google OAuth.
- propiedades TikTok OAuth.
- configuración de roles/permisos.
- configuración de handlers de excepciones.

---

### 3.6. `frontend-angular`

El frontend dejó de ser únicamente un formulario de scoring y pasó a incluir el ciclo completo de autenticación.

Rutas principales:

```text
/login
/register
/verify-email
/mfa/setup
/mfa/verify
/auth/social-callback
/scoring
/terms
/privacy
```

Componentes/capacidades agregadas:

- Login local.
- Registro.
- Verificación de correo.
- MFA setup con QR.
- MFA challenge.
- Google Login.
- TikTok Login.
- AuthGuard.
- GuestGuard.
- Interceptor Bearer.
- recuperación de sesión mediante refresh.
- manejo de errores de autenticación.
- almacenamiento temporal controlado del flujo MFA.
- redirección automática según estado de sesión.

---

## 4. Flujo de registro local implementado

El flujo final es:

```text
Nombre + correo + contraseña
           ↓
      Validaciones
           ↓
      Hash Argon2id
           ↓
        Usuario
           ↓
  código OTP por Gmail
           ↓
    verificar correo
           ↓
 MFA_SETUP_REQUIRED
           ↓
      generar TOTP
           ↓
       mostrar QR
           ↓
Google Authenticator
           ↓
 confirmar código TOTP
           ↓
        usuario ACTIVE
           ↓
 Access JWT + Refresh Session
           ↓
          /scoring
```

### Reanudación del registro

Si el usuario interrumpe el proceso y vuelve a ingresar con la misma cuenta, el sistema intenta continuar desde el estado pendiente en vez de obligarlo a crear otro usuario.

Ejemplos:

```text
PENDING_VERIFICATION
→ reanudar verificación de correo

MFA_SETUP_REQUIRED
→ generar un nuevo token temporal de setup
→ continuar MFA
```

Esto evita duplicar cuentas durante registros incompletos.

---

## 5. Verificación de correo

La verificación de correo se realiza mediante un código temporal enviado por Gmail SMTP.

Características de seguridad:

- código temporal de 6 dígitos;
- expiración;
- un solo uso;
- contador/límite de intentos;
- almacenamiento protegido mediante hash/HMAC;
- pepper de verificación externo;
- no se guarda el código plano en PostgreSQL.

El objetivo es confirmar que el usuario controla la cuenta de correo antes de permitir completar el MFA.

---

## 6. Contraseñas locales

Las contraseñas locales no se almacenan en texto plano.

Se utiliza:

```text
Argon2id
```

El backend es responsable de:

```text
password
   ↓
Argon2id
   ↓
password_hash
   ↓
PostgreSQL
```

La contraseña original nunca debe persistirse ni registrarse en logs.

---

## 7. MFA con Google Authenticator

Se implementó MFA basado en:

```text
TOTP — RFC 6238
```

### Setup

```text
usuario verificado
      ↓
MFA_SETUP token temporal
      ↓
generar secreto TOTP
      ↓
cifrar secreto AES-GCM
      ↓
guardar pending MFA
      ↓
crear URI otpauth://
      ↓
Angular genera QR
      ↓
Google Authenticator
```

La URI tiene el formato conceptual:

```text
otpauth://totp/<issuer>:<usuario>?secret=...&issuer=...
```

Angular renderiza el QR a partir de esa URI.

### Confirmación

```text
Código de 6 dígitos
        ↓
validar setupToken
        ↓
descifrar secret TOTP
        ↓
validar código
        ↓
enabled = true
        ↓
confirmed_at
        ↓
usuario ACTIVE
```

### Protección del secreto TOTP

El secreto TOTP se cifra porque el backend necesita recuperarlo para validar futuros códigos.

Se usa:

```text
AES-GCM
```

La clave AES se obtiene desde un secreto externo, por ejemplo:

```text
file:/app/certs/iam-totp-aes.key
```

No se recomienda almacenar esta clave directamente en el código fuente.

---

## 8. Tokens temporales de autenticación

Se introdujeron tokens temporales separados conceptualmente del Access JWT final.

Principales propósitos:

```text
MFA_SETUP
MFA_CHALLENGE
```

Estos tokens:

- tienen expiración corta;
- están firmados;
- contienen el propósito del flujo;
- identifican al usuario;
- no representan una sesión completamente autenticada;
- no deben utilizarse para acceder al scoring.

El Access JWT final solo se emite después de completar MFA.

---

## 9. JWT propio del sistema

Google o TikTok autentican una identidad externa, pero no sustituyen el token de autorización interno de Motor Scoring.

Después de MFA se emite un JWT propio:

```json
{
  "sub": "<usuario_app_id>",
  "roles": ["USER"],
  "permissions": ["SCORE_CREATE", "SCORE_READ"],
  "mfa": true,
  "iss": "motor-scoring",
  "aud": "motor-scoring-api"
}
```

### Firma

Se utiliza RSA:

```text
iam-private.pem → firma

iam-public.pem  → verificación
```

El backend verifica:

- firma;
- issuer;
- audience;
- expiración;
- sujeto;
- claims requeridos.

---

## 10. Access Token y Refresh Token

La estrategia de sesión quedó separada:

```text
Access JWT
   ↓
vida corta
   ↓
enviado por Authorization: Bearer

Refresh Token
   ↓
vida mayor
   ↓
cookie HttpOnly
   ↓
hash persistido en servidor
```

La cookie de refresh debe usar:

```text
HttpOnly
Secure
SameSite
```

El frontend no necesita acceder al valor del refresh token directamente.

---

## 11. Sesiones y logout

Las sesiones se registran en backend para permitir:

- refresh;
- logout;
- revocación;
- invalidación de sesiones;
- trazabilidad.

El logout realiza conceptualmente:

```text
Cerrar sesión
     ↓
revocar sesión backend
     ↓
invalidar refresh token
     ↓
eliminar refresh cookie
     ↓
limpiar Access JWT
     ↓
volver a /login
```

---

## 12. Protección de rutas Angular

Se implementó comportamiento de sesión activa.

Si el usuario ya tiene una sesión válida y trata de acceder a:

```text
/login
/register
/verify-email
/mfa/setup
/mfa/verify
```

debe ser redirigido a:

```text
/scoring
```

Esto evita que un usuario ya autenticado vuelva al flujo de login/registro.

Si el Access JWT no está disponible pero todavía existe una sesión recuperable mediante refresh cookie, Angular intenta restaurar la sesión antes de mostrar el login.

---

## 13. Cabecera de usuario autenticado

El panel de scoring incorpora una cabecera de sesión.

Conceptualmente:

```text
┌──────────────────────────────────────────────────────┐
│ Motor Scoring          Nombre usuario     Cerrar sesión │
└──────────────────────────────────────────────────────┘
```

Se obtiene información del usuario autenticado mediante:

```text
GET /api/v1/users/me
```

La cabecera permite:

- mostrar el sistema;
- identificar al usuario;
- visualizar correo/nombre;
- cerrar sesión desde cualquier momento del panel.

---

## 14. Social Login con Google

Google se integró mediante:

```text
OAuth 2.0 + OpenID Connect
```

Flujo:

```text
Continuar con Google
        ↓
Google Authorization Endpoint
        ↓
login / consentimiento
        ↓
Authorization Code
        ↓
callback Spring Security
        ↓
validación OIDC
        ↓
Google sub + email
        ↓
buscar/vincular usuario local
        ↓
MFA
        ↓
JWT propio Motor Scoring
        ↓
/scoring
```

Callback local utilizado durante desarrollo:

```text
https://localhost:8443/login/oauth2/code/google
```

### Vinculación por correo

Si el correo de Google ya corresponde a un usuario local verificado, la identidad social puede vincularse con ese usuario en lugar de crear una cuenta duplicada.

Regla de seguridad aplicada:

```text
correo local verificado
+
correo Google verificado
→ permitir vinculación controlada
```

No se debe confiar únicamente en coincidencia textual de un correo no verificado.

---

## 15. Social Login con TikTok

TikTok se integró como OAuth 2.0.

TikTok no se trata como proveedor OIDC.

Flujo:

```text
Continuar con TikTok
        ↓
/api/v1/auth/social/tiktok/start
        ↓
TikTok authorize
        ↓
Authorization Code
        ↓
callback público
        ↓
intercambio de token
        ↓
TikTok user.info.basic
        ↓
provider user id
        ↓
usuario local
        ↓
MFA
        ↓
JWT Motor Scoring
```

Durante desarrollo se utilizó un callback público mediante ngrok.

Ejemplo:

```text
https://unmanned-fedora-pungent.ngrok-free.dev/api/v1/auth/social/tiktok/callback
```

### Sandbox TikTok

En TikTok Sandbox, la cuenta utilizada para autorizar debe estar agregada como:

```text
Target User
```

De lo contrario TikTok puede responder:

```text
non_sandbox_target
```

También se añadió soporte para evitar reutilización automática de una autorización anterior:

```text
disable_auto_auth=1
```

Esto facilita seleccionar explícitamente la cuenta de prueba correcta.

---

## 16. MFA después de Social Login

Ni Google ni TikTok entregan acceso directo al módulo de scoring.

El flujo es:

```text
Proveedor social
      ↓
identidad validada
      ↓
usuario local
      ↓
¿MFA configurado?
    /            \
  NO              SÍ
  ↓               ↓
MFA_SETUP     MFA_CHALLENGE
  ↓               ↓
QR             código TOTP
  \               /
       ↓
 JWT propio
       ↓
   /scoring
```

Esto garantiza que el JWT de Motor Scoring solo se emita después de MFA.

---

## 17. RBAC

Se incorporó autorización basada en roles y permisos.

Modelo base:

```text
USER
 ├── SCORE_CREATE
 └── SCORE_READ

ADMIN
 ├── SCORE_CREATE
 ├── SCORE_READ
 └── USER_ADMIN
```

El JWT puede incluir:

```text
roles
permissions
```

Spring Security utiliza dichos claims para permitir o denegar acceso a recursos protegidos.

---

## 18. Asociación del usuario con el scoring

La solicitud de scoring ya no debe confiar en un `usuarioAppId` enviado libremente desde Angular.

El identificador del usuario se toma del JWT validado:

```text
Access JWT
   ↓
sub = usuario_app_id
   ↓
Spring Security Principal
   ↓
Scoring Controller
   ↓
Application
   ↓
SolicitudScoringWorkflow.usuario_app_id
```

Esto evita que un usuario suplante el identificador de otro mediante un request manipulado.

---

## 19. Modelo de datos IAM

El modelo incluye tablas equivalentes a:

```text
usuarios_app
credenciales_locales
verificaciones_email
identidades_sociales
mfa_totp
sesiones_usuario
roles
permisos
usuario_rol
rol_permiso
```

Relación con scoring:

```text
usuarios_app
     1
     │
     N
solicitud_scoring_workflow
```

Esto permite auditoría y trazabilidad del usuario que generó cada solicitud.

---

## 20. Estados del usuario

El flujo contempla estados como:

```text
PENDING_VERIFICATION
MFA_SETUP_REQUIRED
ACTIVE
BLOCKED
DISABLED
```

Ejemplo:

```text
Registro
   ↓
PENDING_VERIFICATION
   ↓
email confirmado
   ↓
MFA_SETUP_REQUIRED
   ↓
MFA confirmado
   ↓
ACTIVE
```

Los estados permiten reanudar procesos sin duplicar usuarios.

---

## 21. Migraciones Flyway

Las migraciones IAM se agregaron después de las migraciones originales de scoring.

Se mantiene el principio:

```text
NO modificar migraciones históricas ya aplicadas
```

Se incorporaron migraciones para:

- modelo IAM;
- roles/permisos;
- relación usuario/scoring.

En el proyecto integrado actual Flyway contempla las migraciones originales más las migraciones IAM posteriores.

---

## 22. Manejo de excepciones de seguridad

Se separó el manejo de errores IAM del handler genérico.

Objetivo:

```text
Error conocido de autenticación
       ↓
SecurityExceptionHandler
       ↓
HTTP apropiado + código funcional
```

Ejemplos:

```text
INVALID_CREDENTIALS
INVALID_MFA_SETUP_TOKEN
INVALID_MFA_CHALLENGE_TOKEN
MFA_ALREADY_ENABLED
ACCOUNT_NOT_AVAILABLE
EMAIL_ALREADY_EXISTS
EXPLICIT_LINK_REQUIRED
```

El `GlobalExceptionHandler` queda como última barrera para errores no controlados y registra la excepción del backend.

Esto evita ocultar todos los problemas como un genérico:

```text
500 INTERNAL_ERROR
```

---

## 23. Códigos HTTP relevantes

El comportamiento esperado contempla:

```text
200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
409 Conflict
500 Internal Server Error
```

Ejemplos:

```text
credenciales inválidas          → 401
setup token inválido/expirado   → 401
cuenta bloqueada                → 403
correo ya registrado            → 409
MFA ya configurado              → 409
error no controlado             → 500
```

---

## 24. HTTPS y certificados de desarrollo

El stack Docker utiliza TLS.

Durante desarrollo se generan certificados para:

- API;
- PostgreSQL;
- Kafka;
- JWT RSA;
- cifrado TOTP.

Script utilizado:

```bash
bash ./scripts/generate-dev-certs.sh
```

Entre los artefactos generados:

```text
ca.crt
ca.key
api.crt
api.key
api.p12
postgres.crt
postgres.key
Kafka keystores/truststores
iam-private.pem
iam-public.pem
iam-totp-aes.key
iam-verification-pepper.txt
```

La CA de desarrollo puede instalarse como autoridad confiable en el equipo local para evitar errores TLS del navegador.

---

## 25. Kafka con TLS

La integración de IAM no elimina la seguridad Kafka existente.

El stack mantiene Kafka sobre TLS/mTLS.

Se configuraron certificados con uso de clave adecuado para servidor y cliente:

```text
serverAuth
clientAuth
```

El flujo EDA original continúa:

```text
PostgreSQL
   ↓
Outbox
   ↓
Kafka TLS
   ↓
Consumers
```

---

## 26. PostgreSQL

PostgreSQL mantiene los datos de:

```text
Scoring
IAM
Sesiones
MFA
Identidades sociales
RBAC
Outbox
```

La integración IAM se realizó sobre el mismo motor de persistencia, manteniendo las responsabilidades separadas por repositorios/adaptadores.

---

## 27. Docker

El entorno integrado utiliza servicios equivalentes a:

```text
frontend-angular
motor-scoring-api
postgres
kafka
kafka-ui
outbox-publisher
payment-consumer
scoring-consumer
credit-score-delivery-consumer
ngrok (cuando es requerido)
```

El backend recibe secretos mediante variables de entorno y archivos montados en:

```text
/app/certs
```

No se recomienda empaquetar claves privadas dentro de la imagen Docker.

---

## 28. Variables/configuración sensibles

Entre los secretos y parámetros utilizados por IAM se encuentran:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET

TIKTOK_CLIENT_KEY
TIKTOK_CLIENT_SECRET

IAM_TOTP_AES_KEY

IAM JWT private/public key

IAM verification pepper

Gmail SMTP credentials

DB credentials

Kafka TLS credentials
```

Regla:

```text
secretos
  ≠
código fuente
```

Deben suministrarse mediante variables de entorno, secretos del runtime o archivos montados.

---

## 29. Seguridad CORS

Angular y Spring Boot se ejecutan en orígenes distintos durante desarrollo:

```text
Angular:
http://localhost:4200

API:
https://localhost:8443
```

Por ese motivo se configuró CORS para permitir únicamente los orígenes necesarios y soportar credenciales cuando se utiliza la cookie HttpOnly de refresh.

---

## 30. Páginas legales

Se agregaron rutas públicas:

```text
/terms
/privacy
```

Estas páginas también sirven como soporte para la configuración de proveedores OAuth que requieren URLs públicas de términos y privacidad.

---

## 31. UX de autenticación

El login fue rediseñado con identidad visual propia de Motor Scoring.

Elementos principales:

- logotipo;
- título Motor Scoring;
- correo;
- contraseña;
- recuperación visual de errores;
- botón Login;
- separador social;
- botón Google;
- botón TikTok;
- acceso a Crear cuenta.

El MFA muestra:

- QR;
- instrucciones;
- código de 6 dígitos;
- opción de reintentar;
- alternativa cuando no se puede escanear;
- mensajes de error del backend.

---

## 32. Flujo completo final

```text
                    ┌─────────────────────┐
                    │      Angular        │
                    └──────────┬──────────┘
                               │
                 ┌─────────────┴─────────────┐
                 │                           │
                 ▼                           ▼
          Login / Registro             Social Login
             Local                   Google / TikTok
                 │                           │
                 └─────────────┬─────────────┘
                               ▼
                       Usuario identificado
                               │
                               ▼
                      Verificación necesaria
                               │
                               ▼
                             MFA
                               │
                               ▼
                    JWT propio + Refresh Session
                               │
                               ▼
                       Spring Security / RBAC
                               │
                               ▼
                          /scoring
                               │
                               ▼
                   Registrar solicitud scoring
                               │
                               ▼
                   usuario_app_id desde JWT
                               │
                               ▼
                         PostgreSQL
                               │
                               ▼
                      Transactional Outbox
                               │
                               ▼
                            Kafka
                               │
                  ┌────────────┼────────────┐
                  ▼            ▼            ▼
              Payment       Scoring      Delivery
              Consumer      Consumer     Consumer
```

---

## 33. Cambios funcionales principales acumulados

La evolución del proyecto integrado incluyó, entre otros:

1. Integración inicial del modelo IAM sobre Clean Architecture.
2. Separación de adaptadores JPA que tenían colisión de firmas.
3. Corrección de dependencias de testing JUnit.
4. Corrección de imports/configuración Security/Nimbus.
5. Correcciones Kafka mTLS.
6. Escaneo correcto de repositorios JPA IAM.
7. Correcciones frontend Angular/Zone.js.
8. Incorporación de `/terms` y `/privacy`.
9. Reanudación de registro incompleto.
10. Social Login + MFA.
11. Rediseño del login.
12. Persistencia robusta de MFA.
13. Handlers de excepciones con prioridad adecuada.
14. Separación de validación de tokens temporales MFA y Access JWT.
15. Vinculación controlada entre Google y usuario local existente.
16. Correcciones al callback OAuth de Google.
17. Manejo explícito de TikTok Sandbox.
18. Soporte `disable_auto_auth=1` para TikTok.
19. Protección de páginas guest cuando existe sesión activa.
20. Cabecera de usuario autenticado y cierre de sesión.
21. Recuperación de sesión mediante refresh cookie.
22. Asociación segura del usuario autenticado con las solicitudes de scoring.

---

## 34. Consideraciones de despliegue

### Spring Boot ejecutable

En despliegue standalone:

```text
Java 21
   ↓
Spring Boot
   ↓
Tomcat embebido
   ↓
motor-scoring
```

El empaquetado natural es:

```text
JAR
```

### JBoss / WildFly

Si la organización exige JBoss/WildFly, el módulo bootstrap puede prepararse como:

```text
WAR
```

y desplegarse sobre Undertow/JBoss.

Los demás módulos siguen siendo bibliotecas JAR:

```text
motor-scoring-domain          → JAR
motor-scoring-application     → JAR
motor-scoring-infrastructure  → JAR
motor-scoring-presentation    → JAR
motor-scoring-bootstrap       → WAR
```

Debe comprobarse compatibilidad de versión entre:

```text
Spring Boot 3 / Jakarta EE
y
JBoss EAP / WildFly
```

También debe externalizarse:

- HTTPS;
- datasource;
- Kafka;
- OAuth callback URLs;
- claves RSA;
- clave TOTP;
- Gmail;
- secretos sociales.

---

## 35. Pruebas recomendadas

### Build completo

```bash
mvn clean verify
```

### Tests

```bash
mvn test
```

### Package

```bash
mvn clean package
```

### Casos IAM mínimos

```text
registro correcto
registro correo duplicado
código email correcto
código email incorrecto
código email expirado
login correcto
password incorrecto
MFA setup
MFA confirm
TOTP inválido
TOTP expirado
refresh válido
refresh revocado
logout
JWT inválido
JWT expirado
roles insuficientes
Google OAuth
Google + usuario local existente
TikTok OAuth Sandbox
TikTok non_sandbox_target
sesión activa → bloqueo de /login
logout → retorno a /login
scoring protegido
usuario_app_id obtenido desde JWT
```

---

## 36. Criterios de seguridad alcanzados

El diseño final busca garantizar:

- ninguna contraseña en texto plano;
- secreto TOTP cifrado;
- JWT firmado con RSA;
- refresh token no almacenado en texto plano;
- refresh cookie HttpOnly;
- MFA obligatorio antes de emitir el Access JWT final;
- no confiar en `usuario_app_id` enviado por frontend;
- separación de tokens temporales y Access JWT;
- roles/permisos controlados por backend;
- Social Login no equivale automáticamente a autorización final;
- secretos fuera del repositorio;
- errores de seguridad tratados explícitamente;
- dominio de scoring independiente de Spring Security.

---

## 37. Resultado final

El proyecto dejó de ser únicamente un motor de scoring accesible mediante API y pasó a contar con una frontera completa de identidad, autenticación y autorización:

```text
Identidad
   ↓
Verificación
   ↓
MFA
   ↓
Sesión
   ↓
JWT
   ↓
RBAC
   ↓
Scoring
   ↓
Outbox
   ↓
Kafka
```

La integración se realizó conservando el objetivo central de Clean Architecture:

> **El dominio de scoring no conoce cómo se autentica un usuario.  
> La capa de seguridad controla quién puede llegar al caso de uso, mientras el dominio continúa concentrado exclusivamente en las reglas del negocio.**

---

## 38. Próximas mejoras sugeridas

Posibles evoluciones futuras:

- recuperación de contraseña;
- administración de sesiones activas por dispositivo;
- revocación/desvinculación explícita de Google/TikTok;
- MFA recovery codes;
- WebAuthn / Passkeys;
- rotación automatizada de claves JWT;
- Key Management Service / Vault;
- rate limiting distribuido;
- auditoría de eventos de seguridad;
- métricas de intentos fallidos;
- alertas de login sospechoso;
- pruebas E2E automatizadas del ciclo completo;
- pruebas OWASP ASVS;
- cabecera CSP endurecida;
- despliegue productivo detrás de reverse proxy;
- separación de secretos mediante Docker/Kubernetes Secrets;
- observabilidad y trazabilidad IAM.

---

### Documento actualizado

Este README reemplaza el enfoque inicial de “plan de integración” y describe el estado funcional y arquitectónico alcanzado durante la evolución del proyecto.
