#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v openssl >/dev/null || { echo 'Install openssl first'; exit 1; }
if [[ -e .env || -e .secrets ]]; then
 echo 'Existing .env or .secrets detected. Nothing changed; keep your existing credentials.'; exit 1
fi
umask 077
mkdir .secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out .secrets/private.pem 2>/dev/null
openssl pkey -in .secrets/private.pem -pubout -out .secrets/public.pem 2>/dev/null
# Compose secrets must be readable by container UID 10001. The parent directory is owner-only.
chmod 644 .secrets/*.pem
{
 printf 'POSTGRES_PASSWORD=%s\n' "$(openssl rand -hex 24)"
 printf 'AUTH_DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
 printf 'CUSTOMER_DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
 printf 'ORDER_DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
 printf 'BOOTSTRAP_USERNAME=operator\n'
 printf 'BOOTSTRAP_PASSWORD=%s\n' "$(openssl rand -hex 24)"
} > .env
echo 'Local configuration ready. Keep .env and .secrets out of Git.'
