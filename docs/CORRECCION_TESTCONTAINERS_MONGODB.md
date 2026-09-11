# Corrección de Testcontainers MongoDB

## Error corregido

Las pruebas de integración utilizaban:

```java
new MongoDBContainer(DockerImageName.parse("mongo:8.0")).withReplicaSet();
```

La versión de Testcontainers administrada por Spring Boot no expone el método
`withReplicaSet()` en `org.testcontainers.containers.MongoDBContainer`.

## Implementación compatible

```java
new MongoDBContainer(DockerImageName.parse("mongo:8.0"));
```

`MongoDBContainer` configura e inicializa por sí mismo un replica set de un nodo.
La URI para Spring continúa obteniéndose mediante:

```java
MONGO::getReplicaSetUrl
```

## Archivos corregidos

- `FlujoScoringMongoIT.java`
- `SolicitudCreditoMongoE2EIT.java`

No se modificaron `motor-scoring-domain` ni `motor-scoring-application`.
