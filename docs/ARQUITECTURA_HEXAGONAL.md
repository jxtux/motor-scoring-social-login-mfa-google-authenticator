# Arquitectura Hexagonal — Motor de Scoring Crediticio

## Objetivo

Agregar MongoDB como adaptador de salida principal sin modificar el núcleo del proyecto Onion. H2/JPA permanece como adaptador alternativo.

## Hexágono

```text
                     ADAPTADOR DE ENTRADA
                REST Controller / Swagger / Postman
                              │
                      puertos de entrada
                              │
                   ┌──────────▼──────────┐
                   │     APPLICATION     │
                   │ casos de uso        │
                   └──────────┬──────────┘
                              │
                   ┌──────────▼──────────┐
                   │       DOMAIN        │
                   │ entidades, VO,      │
                   │ reglas y scoring    │
                   └──────────┬──────────┘
                              │ puertos de salida
                   ┌──────────┴──────────┐
                   │                     │
          ┌────────▼────────┐   ┌────────▼────────┐
          │ MongoDB         │   │ JPA / H2        │
          │ profile mongodb │   │ profile h2      │
          └─────────────────┘   └─────────────────┘
```

## Reutilización

`motor-scoring-domain` y `motor-scoring-application` permanecen intactos. Las interfaces `Repository` existentes ya funcionan como puertos de salida y los `UseCase` como puertos de entrada.

## Adaptador MongoDB

El módulo `motor-scoring-adapter-out-mongodb` contiene:

- `document`: representación física de documentos.
- `mapper`: conversión Domain ↔ MongoDocument.
- `repository`: repositorios Spring Data MongoDB.
- `adapter`: implementación de puertos del núcleo.
- `sequence`: IDs numéricos compatibles con el dominio.
- `initializer`: datos de referencia equivalentes a Flyway V2/V3.
- `config`: repositorios y transacciones MongoDB.

## Adaptador H2

`motor-scoring-infrastructure` conserva sus clases. Únicamente se agrega `@Profile("h2")` a la configuración JPA y a los adaptadores para evitar que ambos proveedores se registren simultáneamente.

## Selección

```text
profile mongodb → adapters Mongo → MongoTransactionManager
profile h2      → adapters JPA   → JpaTransactionManager
```

`OnionBeanConfiguration` se conserva como Composition Root y continúa creando los servicios puros de Domain/Application. Los decoradores transaccionales también se reutilizan.

## Modelo documental

El modelo de scoring se almacena como agregado con versiones, factores y reglas embebidos. La evaluación contiene los resultados por factor embebidos. Esto evita reproducir artificialmente el esquema relacional dentro de MongoDB.

## Independencia

El contrato REST y el cálculo no cambian. El request de referencia produce `989`, `PREAPROBADA`, versión `1.1.0` y nueve factores en ambas persistencias.
