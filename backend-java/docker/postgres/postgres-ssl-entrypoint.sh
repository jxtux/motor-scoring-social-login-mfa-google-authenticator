#!/bin/sh
set -eu
mkdir -p /var/lib/postgresql/tls
cp /cert-source/postgres-server.crt /var/lib/postgresql/tls/server.crt
cp /cert-source/postgres-server.key /var/lib/postgresql/tls/server.key
cp /cert-source/ca.crt /var/lib/postgresql/tls/ca.crt
chown -R postgres:postgres /var/lib/postgresql/tls
chmod 600 /var/lib/postgresql/tls/server.key
chmod 644 /var/lib/postgresql/tls/server.crt /var/lib/postgresql/tls/ca.crt
exec docker-entrypoint.sh "$@" \
  -c ssl=on \
  -c ssl_cert_file=/var/lib/postgresql/tls/server.crt \
  -c ssl_key_file=/var/lib/postgresql/tls/server.key \
  -c ssl_ca_file=/var/lib/postgresql/tls/ca.crt
