# Motor Scoring — IAM / Seguridad

Overlay de código para integrar lo acordado sin modificar la lógica del dominio de scoring.

## Incluye
- Registro local: nombre + correo + contraseña.
- Verificación de correo con código de 6 dígitos.
- Argon2id.
- Google OAuth 2.0 + OIDC.
- TikTok OAuth 2.0.
- Google Authenticator / TOTP RFC 6238.
- Secreto TOTP cifrado con AES-GCM.
- JWT propio firmado con RSA.
- Spring Security Resource Server.
- Refresh token opaco almacenado como hash y cookie HttpOnly.
- RBAC.
- Angular: registro, verificación, login, MFA, Guard, Interceptor.
- Mejora de errores del formulario de scoring.
- `usuario_app_id` en `solicitud_scoring_workflow`.

## No se modifica
`motor-scoring-domain`: entidades, value objects, fórmulas, reglas y calculadores siguen intactos.

## Nota de integración
El repositorio fuente completo actual no está montado como árbol editable en esta ejecución.
Por eso el ZIP es un overlay sobre la estructura acordada:
`application`, `infrastructure`, `presentation`, `bootstrap` y `frontend-angular`.
Los cambios que dependen de clases existentes de scoring están en `MODIFICACIONES_EXISTENTES.md`.


## Mapa del overlay

```text
backend-java/
├── motor-scoring-application/
│   └── .../application/security/
│       ├── port/in
│       ├── port/out
│       ├── service
│       ├── command
│       └── model
├── motor-scoring-infrastructure/
│   └── .../infrastructure/security/
│       ├── crypto
│       ├── jwt
│       ├── totp
│       ├── oauth
│       ├── email
│       └── persistence
├── motor-scoring-presentation/
│   └── .../presentation/security/
│       ├── controller
│       ├── dto
│       └── exception
└── motor-scoring-bootstrap/
    ├── .../bootstrap/security
    └── resources/
        ├── application-iam.yml
        └── db/migration/V6..V8

frontend-angular/
└── src/app/auth/
    ├── components
    ├── guards
    ├── interceptors
    ├── models
    └── services
```

Consulta `MODIFICACIONES_EXISTENTES.md` antes de tocar las clases actuales de scoring.
