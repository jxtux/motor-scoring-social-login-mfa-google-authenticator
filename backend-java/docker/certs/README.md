# Certificados y secretos IAM de desarrollo

Este directorio se llena ejecutando:

```bash
./scripts/generate-dev-certs.sh
```

El script genera TLS para API/Kafka/PostgreSQL y también material IAM local:

- `iam-private.pem` / `iam-public.pem`: firma y validación del JWT RSA.
- `iam-totp-aes.key`: clave AES-256 para cifrar el secreto TOTP.
- `iam-verification-pepper.txt`: pepper para códigos de verificación.

Todo el material generado es **solo para laboratorio local**, está ignorado por Git y no debe reutilizarse en producción.
