---
name: flyway-migrations
description: Creates Flyway SQL migrations (V<n>__name.sql) and verifies they are compatible by testing them against a throwaway database, without applying them to the user's real database. Use when the user mentions flyway, database migration, schema change, V1__-style scripts, or a migration failure. Not for Liquibase, Prisma, or general code refactoring.
allowed-tools: [Bash, Read, Write, Edit, Grep]
---

# Flyway migrations

Write migration scripts and prove they are compatible. Never apply them to the user's own database. The user runs the real migration later.

## 1. Orient
- Find the migration folder (default `src/main/resources/db/migration`, or `spring.flyway.locations` in `application.yaml`) and list the existing scripts.
- Find the database engine and version in `docker-compose.yml` / `application.yaml`. Test on the same engine and major version.
- Next version = highest existing version + 1. Never reuse or renumber a version.
- Never edit or rename an existing migration. Applied scripts are checksummed, so every schema change goes in a new version.

## 2. Write the script
- Name: `V<next>__<snake_case_description>.sql` (two underscores).
- Start with a short header comment, at most 3 lines: what it changes and why. Add a line if it is destructive or needs a backfill. No per-statement comments unless something is non-obvious.

  ```sql
  -- Add status to orders for fulfilment tracking.
  -- Nullable first, backfilled, then NOT NULL, so existing rows stay valid.
  ```
- Write for existing data: a new NOT NULL column needs a default or a backfill. Split risky changes into steps (add nullable, backfill, add constraint).
- Do not add `IF NOT EXISTS` everywhere. Versioned migrations run once, and blanket guards hide drift. Use it only where a statement is genuinely conditional.
- For destructive statements (DROP TABLE/COLUMN, narrowing a type, TRUNCATE, DELETE without WHERE), stop and confirm with the user before writing them.

## 3. Verify without touching the real database
Use a disposable database, never the one from the project's docker-compose.

1. Start a throwaway container of the same engine and version, on an unused port, with a unique name you remember (example for Postgres):
   `docker run -d --name flyway-check -e POSTGRES_PASSWORD=test -e POSTGRES_DB=check -p 55432:5432 postgres:<version>`
   Wait until it accepts connections. If the project already uses Testcontainers, prefer that.
2. Apply all migrations (existing plus the new one) to this empty database:
   `./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:55432/check -Dflyway.user=postgres -Dflyway.password=test`
   (or the `flyway` CLI with the same url/user/password).
3. Run `flyway:validate` against the same database to check ordering and checksums.
4. Check entity compatibility: run the tests (or start the app context) against the throwaway database with Hibernate validation on, e.g.
   `SPRING_DATASOURCE_URL=... SPRING_JPA_HIBERNATE_DDL_AUTO=validate ./mvnw test`
5. If the migration adds constraints, changes types, backfills or drops things, also test it against data: migrate to the previous version (`-Dflyway.target=<prev>`), insert a few sample rows, then migrate to the latest.
6. Always remove the container when done, even if a step failed: `docker rm -f flyway-check`.

## 4. Report
Keep it short:
- The script path.
- What passed or failed: apply, validate, entity validation, data test.
- Anything destructive or irreversible.
- The command for the user to run when ready (e.g. `./mvnw flyway:migrate`).

On failure, show the exact error, fix only the new script, and re-run on a fresh throwaway database. Never run `repair`, `clean` or `baseline`, and never modify `flyway_schema_history`. If an existing script shows a checksum mismatch, someone edited an applied migration. Tell the user instead of fixing it automatically.

## Rollback
Flyway's free edition has no undo migrations (`U__` scripts are a paid feature), so do not generate them unless the project already uses them. For hard-to-reverse changes, say in the report that rollback needs a backup or a new forward migration, and offer to write that reverse script on request.