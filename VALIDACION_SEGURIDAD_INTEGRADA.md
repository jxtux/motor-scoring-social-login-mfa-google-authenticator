# Validación de la integración IAM sobre el proyecto original

Esta versión parte del proyecto original **motor-scoring-kafka-clean** y agrega IAM/seguridad dentro del mismo árbol. No sustituye Kafka, PostgreSQL, Outbox, consumers, scripts ni la lógica del dominio.

## Validaciones ejecutadas en el entorno de generación

- Java 21: `motor-scoring-domain` compila con `javac`.
- Java 21: `motor-scoring-application` compila con `javac` usando Domain como classpath.
- `motor-scoring-domain` es idéntico archivo por archivo al original.
- Application no importa Spring/Jakarta/JPA.
- Prueba local de TOTP (30 s), AES-256-GCM y HMAC de códigos: OK.
- POM XML: sintaxis válida.
- `docker-compose.yml`, `application.yml`, `application-iam.yml`: YAML válido.
- Scripts Bash: `bash -n` válido.
- JSON/Postman/package.json: sintaxis válida.
- TypeScript agregado: validación sintáctica sin diagnósticos.
- `generate-dev-certs.sh` fue ejecutado y comprobado: genera TLS + RSA JWT + AES TOTP + pepper. Los secretos generados fueron eliminados antes de empaquetar el ZIP.

## Limitación de validación

Este entorno no dispone de Maven ni Docker, y el frontend no contiene `node_modules`, por lo que aquí no se pudo ejecutar `mvn clean verify`, `ng build` ni `docker compose up --build` de punta a punta. El Dockerfile del proyecto sigue realizando el build Maven/Angular al levantar Docker en una máquina con Docker e Internet para resolver dependencias.

## Qué se integró

- Registro local y verificación de correo.
- Argon2id para contraseñas.
- MFA TOTP / Google Authenticator.
- JWT propio RSA/RS256.
- Refresh token rotatorio mediante cookie HttpOnly; en BD se guarda hash.
- RBAC con roles y permisos.
- Google OAuth2 + OpenID Connect.
- TikTok Login Kit OAuth2.
- Spring Security Resource Server.
- Migraciones Flyway IAM V6/V7/V8.
- Asociación del usuario autenticado (`JWT.sub`) a `solicitud_scoring_workflow.usuario_app_id`.
- Angular: rutas de autenticación, guard, interceptor, login local/social y MFA.
- Docker Compose: variables IAM/OAuth añadidas manteniendo los servicios originales.
- Script de certificados: genera también claves/secrets IAM de desarrollo.
- Colección Postman incluida en `backend-java/postman/iam-security`.
