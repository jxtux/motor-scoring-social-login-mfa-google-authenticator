# Plan mínimo de pruebas

## Registro / correo
1. Registrar un usuario nuevo.
2. Confirmar `estado = PENDING_VERIFICATION`.
3. Confirmar que `password_hash` sea Argon2id y nunca contraseña en claro.
4. Código de correo incorrecto incrementa intentos.
5. Después de 5 intentos se bloquea el código.
6. Código expirado se rechaza.
7. Código correcto activa usuario y entrega token `MFA_SETUP`.

## Google Authenticator / TOTP
8. Generar secreto y QR `otpauth://`.
9. Escanear QR con Google Authenticator.
10. Confirmar primer código TOTP.
11. Código actual es aceptado.
12. Tolerancia ±1 ventana de 30 s funciona.
13. Un código fuera de la tolerancia se rechaza.
14. `secret_encrypted` no coincide con el secreto en claro.

## Login / JWT / sesión
15. Contraseña incorrecta incrementa `failed_attempts`.
16. 5 errores bloquean temporalmente 15 minutos.
17. Login correcto sin MFA configurado lleva a `MFA_SETUP`.
18. Login correcto con MFA lleva a `MFA_VERIFY`.
19. Antes del MFA no se emite Access JWT.
20. Después del MFA se emite JWT con:
    - `sub`
    - `roles`
    - `permissions`
    - `mfa=true`
    - `token_use=ACCESS`
    - `iss`, `aud`, `jti`, `exp`.
21. Modificar un byte del JWT debe producir 401.
22. Refresh rota el refresh token y revoca el anterior.
23. Logout revoca la sesión.

## RBAC / scoring
24. USER con `SCORE_CREATE` puede registrar scoring.
25. Usuario sin `SCORE_CREATE` recibe 403.
26. `usuario_app_id` de la solicitud se obtiene de `JWT.sub`.
27. Un ID enviado maliciosamente desde Angular nunca reemplaza `JWT.sub`.
28. "Mis solicitudes" filtra por el usuario autenticado.

## Social login
29. Google usa `sub` como `provider_user_id`.
30. TikTok usa `open_id` como `provider_user_id`.
31. Si ya existe el mismo email pero no está vinculada la identidad social,
    no se vincula automáticamente.
32. Google/TikTok convergen al mismo MFA interno de FinanScore.

## Regresión
33. `motor-scoring-domain` no recibe Spring/JPA/OAuth/JWT/TOTP.
34. Las fórmulas y reglas de scoring no cambian.
35. Outbox y Kafka continúan publicando/consumiendo como antes.
36. El formulario Angular muestra campos inválidos y `violations` del backend.
