# Validación estática del overlay

- Archivos Java: 86
- Archivos TypeScript: 14
- Migraciones SQL: 3
- Dependencias Spring/Jakarta dentro de application: 0
- Archivos generados dentro de motor-scoring-domain: 0
- Archivos Java con desbalance simple de llaves: 0

## Resultado

OK: Application se mantiene sin Spring/Jakarta.

OK: No se generó ningún archivo en motor-scoring-domain.

OK: chequeo simple de llaves Java sin incidencias.

> Este chequeo no sustituye `mvn clean verify` ni `npm test/build` dentro del repositorio real,
> porque el árbol completo actual y sus POM/package.json no están montados en este entorno.
