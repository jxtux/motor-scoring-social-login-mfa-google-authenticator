# Cambios sobre scoring existente

## `RegistrarSolicitudScoringCommand`
Agregar `Long usuarioAppId`.

## `ScoringRequestController`
No aceptar `usuarioAppId` desde Angular. Obtenerlo del JWT:

```java
@PostMapping
@PreAuthorize("hasAuthority('SCORE_CREATE')")
public ResponseEntity<?> registrar(
        @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,
        @Valid @RequestBody ScoringRequest request) {

    Long usuarioAppId = Long.valueOf(jwt.getSubject());
    RegistrarSolicitudScoringCommand command = mapper.toCommand(request, usuarioAppId);
    return ResponseEntity.accepted().body(useCase.registrar(command));
}
```

## `SolicitudScoringWorkflow`
Agregar:
```java
private Long usuarioAppId;
```

## `SolicitudScoringWorkflowJpaEntity`
Agregar:
```java
@Column(name="usuario_app_id")
private Long usuarioAppId;
```
y mapearlo.

## Consultas
"Mis solicitudes" debe filtrar por `usuario_app_id = JWT.sub`.

## Dominio
No modificar entidades, value objects, fórmulas, calculadores ni reglas crediticias.

## Kafka / Outbox
No se cambia el flujo. `actorUserId` puede agregarse al payload solo como trazabilidad opcional.
