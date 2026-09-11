# Manifiesto de cambios respecto al proyecto original

Generado automáticamente comparando el proyecto integrado con el original.

```text
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/.env.example and /mnt/data/motor-scoring-kafka-clean-iam-integrated/.env.example differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated: MANIFEST_INTEGRACION_IAM.md
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/README.md and /mnt/data/motor-scoring-kafka-clean-iam-integrated/README.md differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated: README_SEGURIDAD_IAM.md
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated: VALIDACION_SEGURIDAD_INTEGRADA.md
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/docker/certs/README.md and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/docker/certs/README.md differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application/command/RegistrarSolicitudScoringCommand.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application/command/RegistrarSolicitudScoringCommand.java differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application/model/SolicitudScoringWorkflow.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application/model/SolicitudScoringWorkflow.java differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application: security
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application/service/RegistrarSolicitudScoringService.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-application/src/main/java/com/finanscore/motorscoring/application/service/RegistrarSolicitudScoringService.java differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-bootstrap/pom.xml and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/pom.xml differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/src/main/java/com/finanscore/motorscoring/bootstrap: security
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/src/main/resources: application-iam.yml
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-bootstrap/src/main/resources/application.yml and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/src/main/resources/application.yml differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/src/main/resources/db/migration: V6__crear_modelo_seguridad_iam.sql
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/src/main/resources/db/migration: V7__seed_roles_permisos.sql
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-bootstrap/src/main/resources/db/migration: V8__agregar_usuario_a_solicitud_scoring.sql
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-infrastructure/pom.xml and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-infrastructure/pom.xml differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-infrastructure/src/main/java/com/finanscore/motorscoring/infrastructure/persistence/adapter/SolicitudScoringWorkflowRepositoryAdapter.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-infrastructure/src/main/java/com/finanscore/motorscoring/infrastructure/persistence/adapter/SolicitudScoringWorkflowRepositoryAdapter.java differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-infrastructure/src/main/java/com/finanscore/motorscoring/infrastructure/persistence/entity/SolicitudScoringWorkflowJpaEntity.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-infrastructure/src/main/java/com/finanscore/motorscoring/infrastructure/persistence/entity/SolicitudScoringWorkflowJpaEntity.java differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-infrastructure/src/main/java/com/finanscore/motorscoring/infrastructure: security
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-infrastructure/src: test
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-presentation/pom.xml and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-presentation/pom.xml differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-presentation/src/main/java/com/finanscore/motorscoring/presentation/config/CorsConfiguration.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-presentation/src/main/java/com/finanscore/motorscoring/presentation/config/CorsConfiguration.java differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/backend-java/motor-scoring-presentation/src/main/java/com/finanscore/motorscoring/presentation/controller/ScoringRequestController.java and /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-presentation/src/main/java/com/finanscore/motorscoring/presentation/controller/ScoringRequestController.java differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/motor-scoring-presentation/src/main/java/com/finanscore/motorscoring/presentation: security
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/backend-java/postman: iam-security
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/docker-compose.yml and /mnt/data/motor-scoring-kafka-clean-iam-integrated/docker-compose.yml differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/docs: security-iam
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/frontend-angular/Dockerfile and /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/Dockerfile differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/docker: nginx.conf
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/frontend-angular/package.json and /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/package.json differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/frontend-angular/src/app/app.component.html and /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/app/app.component.html differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/frontend-angular/src/app/app.component.ts and /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/app/app.component.ts differ
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/app: app.routes.ts
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/app: auth
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/app: config
Only in /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/app: scoring
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/frontend-angular/src/main.ts and /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/main.ts differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/frontend-angular/src/styles.css and /mnt/data/motor-scoring-kafka-clean-iam-integrated/frontend-angular/src/styles.css differ
Files /mnt/data/merge_work/current/orig/motor-scoring-kafka-clean/scripts/generate-dev-certs.sh and /mnt/data/motor-scoring-kafka-clean-iam-integrated/scripts/generate-dev-certs.sh differ
```
