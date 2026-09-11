# Validación ejecutada durante la generación

Se realizaron estas comprobaciones dentro del entorno de generación:

1. Validación XML de todos los `pom.xml`.
2. Validación YAML de profiles, Docker Compose y OpenAPI.
3. Validación JSON de Postman y environments.
4. Comparación SHA-256 de `domain/src` y `application/src`: sin diferencias.
5. Compilación con `javac` de Presentation, Infrastructure H2 y Bootstrap contra las dependencias del JAR Onion original.
6. Compilación con `javac` del nuevo adaptador MongoDB usando stubs equivalentes de las APIs externas para comprobar sintaxis y uso del dominio.
7. Ejecución directa del núcleo con el modelo MongoDB inicializado: versión `1.1.0`, 9 factores, score `989`, resultado `PREAPROBADA`.

No se pudo ejecutar `mvn clean verify` en el entorno de generación porque Maven no estaba instalado y el gestor de paquetes no tuvo acceso de red. El proyecto incluye Dockerfile con Maven 3.9.9 y las pruebas quedan listas para ejecutarse con:

```bash
docker build .
```

o localmente:

```bash
mvn clean verify
```
