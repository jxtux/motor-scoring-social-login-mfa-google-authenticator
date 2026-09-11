#!/usr/bin/env sh
set -eu
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"
[ -f .env ] || cp .env.example .env
[ -f backend-java/docker/certs/ca.crt ] || ./scripts/generate-dev-certs.sh
docker compose up -d --build
docker compose ps
