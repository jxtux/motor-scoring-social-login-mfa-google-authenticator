# Checklist de integración

1. Copiar los paquetes nuevos de `application`.
2. Hacer que `infrastructure` dependa de `application`.
3. Agregar dependencias Maven indicadas en `backend-java/POM_DEPENDENCIES_TO_ADD.md`.
4. Copiar `infrastructure/security`.
5. Copiar controllers de `presentation/security`.
6. Copiar configuración de `bootstrap/security`.
7. Importar `application-iam.yml` desde el `application.yml` existente.
8. Aplicar Flyway V6, V7 y V8 en PostgreSQL.
9. Integrar `usuarioAppId` en command/workflow/JPA mapper de scoring.
10. Proteger el endpoint de scoring con `SCORE_CREATE`.
11. Instalar `qrcode` en Angular.
12. Integrar rutas, Guard e Interceptor sin borrar rutas existentes.
13. Aplicar el patch de validación del formulario de scoring.
14. Configurar RSA, AES, SMTP, Google y TikTok mediante secretos.
15. Ejecutar el plan de pruebas.

## No tocar
- `motor-scoring-domain`.
- Fórmulas/reglas de scoring.
- Transactional Outbox.
- Productores/consumidores Kafka salvo metadato opcional de trazabilidad.
- Configuración TLS/Kafka/PostgreSQL ya existente, excepto añadir las variables IAM necesarias.
