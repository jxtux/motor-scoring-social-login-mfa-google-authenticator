# Secretos

No versionar valores reales:

- `IAM_RSA_PRIVATE_KEY`
- `IAM_RSA_PUBLIC_KEY`
- `IAM_TOTP_AES_KEY`
- `IAM_VERIFICATION_PEPPER`
- SMTP credentials
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `TIKTOK_CLIENT_KEY`, `TIKTOK_CLIENT_SECRET`

RSA local:
```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out iam-private.pem
openssl rsa -pubout -in iam-private.pem -out iam-public.pem
```

AES-256:
```bash
openssl rand -base64 32
```
