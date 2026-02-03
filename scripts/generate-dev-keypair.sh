#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
private_key_path="${root_dir}/dev-private.pem"
public_key_path="${root_dir}/src/main/resources/dev-public.pem"

openssl genrsa -out "${private_key_path}" 2048
openssl rsa -in "${private_key_path}" -pubout -out "${public_key_path}"

echo "Generated dev-private.pem and dev-public.pem."
