# petshop-api

Backend of the Petshop Compet system — Spring Boot 3.5, Java 17, PostgreSQL 16, Flyway.

## Requirements

- Java 17 (the Maven wrapper `mvnw` downloads Maven)
- Docker (dev database and tests)
- gitleaks, for the pre-commit hook: `winget install Gitleaks.Gitleaks`

## First-time setup

```bash
git config core.hooksPath .githooks              # secret scan on every commit
cp .env.example .env                             # then edit the values
docker compose -f docker-compose.dev.yml up -d   # PostgreSQL 16 on localhost:5434
sh mvnw spring-boot:run                          # Flyway creates the schema on first start
```

The API listens on http://localhost:8080. Log in with `APP_OWNER_EMAIL` and
`APP_ADMIN_PASSWORD` from your `.env`; create other accounts under "Usuários".

## Configuration

Secrets have **no defaults**: the app refuses to start if one is missing. Locally they come
from `.env` (git-ignored), which the app imports on startup; real environment variables
override it. In production they come from AWS Secrets Manager.

| Variable | Required | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_PASSWORD` | yes | Database password |
| `JWT_SECRET` | yes | Signs login tokens (32+ characters) |
| `APP_OWNER_EMAIL` | first start | E-mail of the first OWNER account, created while the users table is empty |
| `APP_OWNER_NAME` | no | Name of that account (default "Proprietário") |
| `APP_ADMIN_PASSWORD` | first start | Initial password of that OWNER account |
| `ANTHROPIC_API_KEY` | yes | AI features (any value if unused) |
| `SPRING_DATASOURCE_URL` | no | Default `jdbc:postgresql://localhost:5434/petshop` (the dev database) |
| `SPRING_DATASOURCE_USERNAME` | no | Default `postgres` |
| `SPRING_FLYWAY_USER` / `SPRING_FLYWAY_PASSWORD` | prod only | Schema-owner role used by Flyway; locally Flyway uses the datasource user |
| `SPRING_JPA_SHOW_SQL` | no | `true` to print SQL locally |

`SPRING_PROFILES_ACTIVE=prod` switches logs to JSON.

## Database migrations

The schema is owned by Flyway: `src/main/resources/db/migration/V{n}__description.sql`.
Hibernate only validates it (`ddl-auto: validate`). Never edit a migration that has been
deployed — add a new one. Every entity change needs a matching migration.

## Tests

```bash
sh mvnw verify
```

Needs Docker running: tests that touch the database start a throwaway PostgreSQL 16
(Testcontainers), run the Flyway migrations and validate the entities against them.
CI (`.github/workflows/ci.yml`) runs the same command plus a gitleaks scan on every push.

## Deploy

Push to `develop` → GitHub Actions builds, tests and deploys to the EC2 server, rolling back
automatically if the new version fails to start. Secrets, database roles, logs and
troubleshooting are documented in `OPERATIONS.md` (project folder, next to `SAAS-ROADMAP.md`).
