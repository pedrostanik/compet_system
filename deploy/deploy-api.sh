#!/usr/bin/env bash
# Runs ON THE EC2 INSTANCE. Invoked by .github/workflows/deploy.yml after the
# new jar, compose file and scripts have been uploaded to $STAGING.
#
# Swaps in the new jar, refreshes secrets, recreates the api container, and
# rolls back to the previous jar if the app does not start.
set -euo pipefail

APP_DIR=/home/ubuntu/petshop
STAGING="${STAGING:-$APP_DIR/.deploy-staging}"
JAR="$APP_DIR/petshop-api/target/petshop-api-0.0.1-SNAPSHOT.jar"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-180}"

if docker compose version >/dev/null 2>&1; then
  compose() { docker compose "$@"; }
else
  compose() { docker-compose "$@"; }
fi

cd "$APP_DIR"

echo "==> Installing compose file and scripts"
[ -f docker-compose.yml ] && cp docker-compose.yml "docker-compose.yml.bak"
install -m 644 "$STAGING/docker-compose.yml" docker-compose.yml
install -m 755 "$STAGING/load-secrets.sh" load-secrets.sh

echo "==> Loading secrets from Secrets Manager"
./load-secrets.sh "$APP_DIR/.env.secrets"

echo "==> Swapping jar (previous kept as .prev)"
[ -f "$JAR" ] && cp "$JAR" "$JAR.prev"
# mv gives the file a new inode; the container is recreated below so it picks it up.
mv "$STAGING/app.jar" "$JAR.new"
mv "$JAR.new" "$JAR"

wait_for_start() {
  local since="$1"
  local deadline=$((SECONDS + STARTUP_TIMEOUT))
  while (( SECONDS < deadline )); do
    if ! docker ps --format '{{.Names}}' | grep -qx petshop-api; then
      echo "petshop-api container is not running"
      return 1
    fi
    if docker logs --since "$since" petshop-api 2>&1 | grep -q "Started PetshopApiApplication"; then
      return 0
    fi
    if docker logs --since "$since" petshop-api 2>&1 | grep -q "APPLICATION FAILED TO START"; then
      return 1
    fi
    sleep 3
  done
  echo "Timed out after ${STARTUP_TIMEOUT}s waiting for startup"
  return 1
}

echo "==> Recreating api container"
started_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
compose --env-file "$APP_DIR/.env.secrets" up -d --force-recreate --no-deps api #compose up -d --force-recreate --no-deps api

if wait_for_start "$started_at"; then
  echo "==> Deploy OK"
  rm -rf "$STAGING"
  exit 0
fi

echo "!!! New version failed to start. Last log lines:"
docker logs --tail 80 petshop-api 2>&1 || true

if [ -f "$JAR.prev" ]; then
  echo "==> Rolling back to previous jar"
  cp "$JAR.prev" "$JAR"
  started_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  compose --env-file "$APP_DIR/.env.secrets" up -d --force-recreate --no-deps api #compose up -d --force-recreate --no-deps api
  if wait_for_start "$started_at"; then
    echo "==> Rollback OK (previous version is running)"
  else
    echo "!!! Rollback also failed — manual intervention needed"
  fi
fi
exit 1
