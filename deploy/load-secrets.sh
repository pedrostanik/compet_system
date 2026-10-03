#!/usr/bin/env bash
# Pulls the JSON secret from AWS Secrets Manager and writes it as a compose env file.
# Runs ON THE EC2 INSTANCE, authenticated by the instance role — secrets never pass through GitHub.
#
# The instance role needs: secretsmanager:GetSecretValue on the secret's ARN.
set -euo pipefail

SECRET_ID="${SECRET_ID:-pet_system_secrets}"
AWS_REGION="${AWS_REGION:-us-east-2}"
OUT_FILE="${1:-/home/ubuntu/petshop/.env.secrets}"

tmp="$(mktemp "${OUT_FILE}.XXXXXX")"
trap 'rm -f "$tmp"' EXIT
chmod 600 "$tmp"

aws secretsmanager get-secret-value \
  --secret-id "$SECRET_ID" \
  --region "$AWS_REGION" \
  --query SecretString \
  --output text \
| python3 -c '
import json, sys
data = json.load(sys.stdin)
required = ["SPRING_DATASOURCE_PASSWORD", "SPRING_FLYWAY_PASSWORD", "JWT_SECRET", "ANTHROPIC_API_KEY", "APP_ADMIN_PASSWORD"]
missing = [k for k in required if not data.get(k)]
if missing:
    sys.exit("secret is missing keys: " + ", ".join(missing))
for key, value in data.items():
    value = str(value)
    # Written unquoted: docker-compose v1 keeps quotes as part of the value, v2 strips them.
    # Unquoted is read identically by both, as long as the value avoids the characters below.
    bad = [c for c in ("$", "\n", "\x27", "\"", " #") if c in value]
    if value != value.strip():
        bad.append("leading/trailing space")
    if bad:
        sys.exit(key + ": value contains characters the env file cannot hold portably: " + repr(bad))
    print(f"{key}={value}")
' > "$tmp"

mv "$tmp" "$OUT_FILE"
trap - EXIT
echo "Wrote $(wc -l < "$OUT_FILE") secrets to $OUT_FILE"
