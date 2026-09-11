# Cambios realizados para Arquitectura Hexagonal

## Conservado sin cambios

Se verificó por SHA-256 que todos los archivos bajo estos directorios son idénticos al ZIP Onion original:

```text
motor-scoring-domain/src
motor-scoring-application/src
```

También se conservaron los comentarios existentes en las clases Onion que permanecen en el proyecto.

## Cambios mínimos en H2/JPA

- Se mantiene `motor-scoring-infrastructure`.
- Se agrega `@Profile("h2")` a sus cinco adaptadores y a `JpaPersistenceConfiguration`.
- No se reescribe la lógica JPA ni se eliminan migraciones Flyway.

## Nuevo adaptador MongoDB

Se agrega `motor-scoring-adapter-out-mongodb` con documentos, mappers, repositorios Spring Data, implementaciones de los cinco puertos, secuencias numéricas, transacciones e inicialización de datos.

## Bootstrap

- MongoDB es el profile predeterminado.
- H2 se activa con `--spring.profiles.active=h2`.
- `OnionBeanConfiguration` conserva su nombre y sigue siendo el Composition Root.
- Los decoradores transaccionales originales se reutilizan con `MongoTransactionManager` o `JpaTransactionManager`.

## Operación y pruebas

- Docker Compose MongoDB con replica set.
- Docker Compose H2 alternativo.
- OpenAPI y Postman actualizados.
- Integración y E2E para ambos proveedores.
- ArchUnit con reglas hexagonales.
