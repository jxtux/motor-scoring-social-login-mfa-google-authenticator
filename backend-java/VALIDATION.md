# Validación del proyecto hexagonal

## Validaciones automáticas previstas

```bash
mvn clean test
mvn clean verify
```

`verify` ejecuta unitarias, integración, E2E y ArchUnit. Las pruebas MongoDB usan Testcontainers con replica set y se omiten si Docker no está disponible.

## Resultado funcional esperado

Para el request de referencia:

```text
score: 989
resultado: PREAPROBADA
versión: 1.1.0
factores: 9
```

Debe obtenerse tanto con `mongodb` como con `h2`.

## Integridad del núcleo

Se compara la huella SHA-256 de todos los archivos bajo:

```text
motor-scoring-domain/src
motor-scoring-application/src
```

con el proyecto Onion original. Deben ser idénticos.
