-- One-time setup (2026-10-03): split the database roles.
--   petshop_migrator  owns the schema; used only by Flyway (SPRING_FLYWAY_USER).
--   app_user          used by the running app; SELECT/INSERT/UPDATE/DELETE only, cannot create or alter tables.
-- This is also what Phase 2 RLS needs: policies do not apply to the table owner.
--
-- Usage, on the database server:  sudo -u postgres psql -d petshop -f db-roles.sql
-- Run as a superuser, connected to the petshop database (prompt must end in =#).
\set ON_ERROR_STOP on
SELECT current_database() = 'petshop' AS in_petshop_db \gset
\if :in_petshop_db
\else
  \echo 'Not connected to petshop - run: sudo -u postgres psql -d petshop'
  \quit
\endif

BEGIN;
-- 1. Owner role for migrations. It cannot authenticate until \password (step 5) sets a password.
CREATE ROLE petshop_migrator LOGIN;
GRANT CONNECT ON DATABASE petshop TO petshop_migrator;
GRANT USAGE, CREATE ON SCHEMA public TO petshop_migrator;

-- 2. Hand the 12 tables (and their identity sequences) from postgres to petshop_migrator.
--    Existing grants to app_user are kept.
DO $$
DECLARE t text;
BEGIN
  FOR t IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tableowner = 'postgres' LOOP
    EXECUTE format('ALTER TABLE public.%I OWNER TO petshop_migrator', t);
  END LOOP;
END $$;

-- 3. app_user keeps data-only access, including to tables created by future migrations.
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
ALTER DEFAULT PRIVILEGES FOR ROLE petshop_migrator IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES FOR ROLE petshop_migrator IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO app_user;
COMMIT;

-- 4. Check: every table owned by petshop_migrator, app_user can read/write but not create.
SELECT tablename, tableowner,
       has_table_privilege('app_user', 'public.' || tablename, 'INSERT') AS app_user_can_write
FROM pg_tables WHERE schemaname = 'public' ORDER BY 1;
SELECT has_schema_privilege('app_user', 'public', 'CREATE') AS app_user_can_create;

-- 5. Set the migrator's password (prompted, never echoed or saved in history).
\password petshop_migrator
