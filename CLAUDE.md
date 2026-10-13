# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`knowra` — minimal Spring Boot 4.1.1 (Java 21) app (`KnowraApplication.java` only). Maven wrapper (`mvnw`). `pom.xml` includes `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-ai-starter-model-openai`, `spring-ai-starter-vector-store-pgvector`, `jjwt`, Lombok, ModelMapper, Flyway, PostgreSQL, `springdoc-openapi`.

No multi-service code exists yet (no auth controllers, no notification service, no gateway filters, no `com.monitoring.*` packages). Only source files are `KnowraApplication.java` and its test. `application.yaml` and `V1__create_vector_store.sql` define the DB/config setup.

## Commands

- Run: `./mvnw spring-boot:run`
- Tests: `./mvnw test`; single test: `./mvnw test -Dtest=KnowraApplicationTests`
- Format: `./mvnw -q spotless:apply` (run by `.claude/hooks/format-java.sh` after edits)
- DB: PostgreSQL (`pgvector/pgvector:pg16`), port `5434`, DB `knowra`. Migration: `src/main/resources/db/migration/V1__create_vector_store.sql`.

## What's actually in this repo

- `pom.xml`: Spring Boot 4.1.1, Java 21, dependencies listed above.
- `src/main/java/io/github/lakshaybxt/knowra/KnowraApplication.java`: single `@SpringBootApplication` main class.
- `src/test/java/io/github/lakshaybxt/knowra/KnowraApplicationTests.java`: basic context test.
- `src/main/resources/application.yaml`: `spring.datasource` (postgres `localhost:5434/knowra`), `spring.jpa` (`ddl-auto: validate`), Flyway enabled, Spring AI OpenAI config (`api-key` from env `OPENAI_API_KEY`), pgvector settings (`table-name: vector_store`, `dimensions: 1536`), app RAG settings (`chunk-size: 600`, `chunk-overlap: 100`, `top-k: 5`), logging.
- `src/main/resources/db/migration/V1__create_vector_store.sql`: creates `vector` and `uuid-ossp` extensions; `vector_store` table (`id UUID`, `content TEXT`, `metadata JSON`, `embedding VECTOR(1536)`).
- `.claude/hooks/`: `format-java.sh` (spotless), `run-tests.sh` (`mvnw test`), `protect-files.sh`, `block-destructive.sh`.
- `.claude/commands/add-logging.md`: logging rules (`@Slf4j`, parameterized messages, no `console.log`/`printStackTrace`).
- `.claude/agents/spring-boot-reviewer.md`: review checklist for Spring Boot / JPA / Kafka issues (relevant if services are added later).
- `.claude/settings.local.json`: hook config (tests run on stop, format on edit, destructive-block on Bash).

## What is NOT here

There is no `coding-standards.md` in `.claude/rules/` (directory exists but is empty). There are no auth controllers, gateway filters, notification services, Eureka server code, React frontend, or `com.monitoring.*` packages. If those are added, follow the patterns in the existing files (`application.yaml` config, Lombok in `pom.xml`, Flyway migrations, JWT / Spring Security dependencies, pgvector table structure).
