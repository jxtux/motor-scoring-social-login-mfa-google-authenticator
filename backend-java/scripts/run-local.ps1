$ErrorActionPreference = "Stop"
$Root = Resolve-Path "$PSScriptRoot\..\.."
Set-Location $Root
if (-not (Test-Path ".env")) { Copy-Item ".env.example" ".env" }
if (-not (Test-Path "backend-java\docker\certs\ca.crt")) { & ".\scripts\generate-dev-certs.sh" }
docker compose up -d --build
docker compose ps
