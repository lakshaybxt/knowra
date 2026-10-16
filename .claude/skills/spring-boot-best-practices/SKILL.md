---
name: spring-boot-best-practices
description: Write, review, and clean up Java Spring Boot code to production-grade standards - layering, DTOs, Jakarta Bean Validation, global exception handling (ProblemDetail), meaningful comments/Javadoc, JPA and transaction hygiene, configuration, security, logging, testing, and removal of dead and duplicate code. Use this skill whenever the user is writing, reviewing, refactoring, or auditing any Spring Boot / Spring MVC / Spring Data / Spring Security Java code, mentions controllers, services, repositories, entities, DTOs, @Valid, @Transactional, application.yml, or asks for "best practices", "code review", "clean up", "remove unused code", "find duplicates", or "make this production ready" for a Java backend - even if they never say "Spring Boot" explicitly.
compatibility: Java 17+, Spring Boot 3.x and 4.x, Maven or Gradle. Static analysis steps use Maven plugins (PMD/CPD, Checkstyle, SpotBugs) when a shell is available.
---

# Spring Boot Best Practices

Standards for writing, reviewing, and cleaning Spring Boot backends. The goal is code that a new teammate can read, change safely, and trust in production. Each rule below exists because ignoring it causes a specific, recurring failure (a leaked entity, an N+1 nobody noticed, a stale comment that lies, a 500 where a 400 belonged). Understanding the reason lets you apply the rule with judgment instead of mechanically.

## Choose a mode

Decide which of these the user wants, then follow that section. If unclear, default to the one that matches the files they shared.

| Mode | Trigger | What to do |
|---|---|---|
| **Write** | New feature, endpoint, class | Apply the conventions below while writing. Don't lecture; the code is the deliverable. |
| **Review** | "Review", "audit", "is this good?" | Scan against the checklists, report findings (see *Review output*), then offer to fix. |
| **Clean up** | "Remove unused/duplicate code", "refactor" | Follow `references/code-hygiene.md`. Detect first, verify, then delete or extract. |

## Before touching code

1. **Detect the stack.** Read `pom.xml` / `build.gradle`: Boot version, Java version, starters (`spring-boot-starter-webmvc` vs `-web`, JPA, security, validation), Lombok, MapStruct, test libs. Boot 4 differs from 3 in ways that break compilation (see *Boot 3 vs 4*).
2. **Match existing conventions.** If the project already has a package layout, naming scheme, or error format, extend it instead of introducing a second style. Mixed styles are worse than a mediocre consistent one. Flag disagreements; don't silently refactor unrelated code.
3. **Keep changes scoped.** Fix what was asked plus defects in the code you touch. Report unrelated findings; don't rewrite them.

## Core rules

### 1. Structure and layering
- Package **by feature** (`com.acme.order`, `com.acme.customer`) once the app has more than a handful of classes; everything about orders lives together. Package-by-layer is fine for small services. Never use the default package (component scanning breaks).
- Put `@SpringBootApplication` in the root package so scanning covers all sub-packages.
- Dependencies point inward: controller -> service -> repository. A service never imports a controller or `HttpServletRequest`; a repository never contains business rules.
- **Controller**: HTTP only - bind, validate, call one service method, map to a response. No business logic, no repository calls, no transactions.
- **Service**: business rules and transaction boundaries. Stays free of web types so it is testable without Spring.
- **Repository**: queries only.
- GraphQL controllers (`@QueryMapping`, `@MutationMapping`) follow the same layering: thin resolvers, validated `@Argument` inputs, no entities in schema types.

### 2. DTOs and mapping
- Never expose a JPA `@Entity` from an API or accept one as a request body. It leaks columns, couples the contract to the schema, invites mass-assignment, and triggers lazy-loading surprises during serialization.
- Use Java `record`s for request/response DTOs (immutable, no boilerplate). Separate request and response types; avoid one DTO for create, update, and read.
- Map in one place (MapStruct or a small mapper/`from` factory), not inline in controllers.

### 3. Dependency injection
- Constructor injection only; fields `private final`. One constructor needs no `@Autowired`. Field injection hides dependencies and forces reflection in tests.
- A constructor with more than ~5 dependencies signals a class doing too much; split it.
- Lombok `@RequiredArgsConstructor` is acceptable if the project already uses Lombok. Never put `@Data` or `@EqualsAndHashCode` on entities.

### 4. Validation (summary - details in `references/api-validation-errors.md`)
- Validate at the boundary: constraint annotations on request DTOs plus `@Valid @RequestBody` on the controller parameter. Without `@Valid` the annotations are silently ignored.
- Nested objects and collections need `@Valid` on the field (`List<@Valid ItemRequest>` or `@Valid List<ItemRequest> items`), otherwise they are not cascaded.
- Path/query params: put constraints directly on the parameter (`@PathVariable @Positive Long id`). With class-level `@Validated` you get `ConstraintViolationException`; without it, Spring 6.1+ built-in method validation throws `HandlerMethodValidationException`. Handle whichever your version produces.
- `spring-boot-starter-validation` must be on the classpath; it is not transitive.
- Bean Validation checks shape (null, size, format). Business rules that need data (unique email, stock available) belong in the service and throw domain exceptions.
- Write `message` text a client can act on; don't leave default messages in a public API.

### 5. Error handling
- One `@RestControllerAdvice` translates exceptions to **RFC 9457 `ProblemDetail`** responses. Same error shape for every endpoint.
- Throw specific domain exceptions from services (`ResourceNotFoundException`, `DuplicateResourceException`) - not `RuntimeException`, not `Exception`.
- Never leak stack traces, SQL, or internal class names to clients. The catch-all handler logs the full exception once and returns a generic 500.
- Never return HTTP 200 with an error body. Use the status that matches: 400 invalid input, 401/403 auth, 404 missing, 409 conflict, 422 semantic failure, 500 unexpected.
- Never swallow exceptions (`catch (Exception e) {}`) and never log-and-rethrow (the error then appears twice). Handle it or let it propagate.

### 6. Comments and Javadoc
Comments are for what the code cannot say: **why**, constraints, and non-obvious consequences. The name says what, the code says how, the comment says why.
- Javadoc every public class and every public method of a service/API surface: one-sentence summary (complete sentence, ends with a period), `@param`/`@return`/`@throws` only when they add information beyond the name and type.
- Skip Javadoc on getters, setters, trivial overrides, and self-evident methods. Boilerplate Javadoc is noise that trains readers to ignore comments.
- Delete comments that restate code (`// increment i`), banners, changelog comments, `@author`/date tags, and **commented-out code** (version control remembers it).
- Update or delete a comment when you change the code beneath it. A stale comment is worse than none because it is trusted.
- `TODO`/`FIXME` must carry a ticket or owner (`// TODO(PROJ-123): ...`). Orphan TODOs rot.
- Document non-obvious business rules, workarounds (with a link to the bug), concurrency assumptions, and units.

### 7. Persistence and transactions (details in `references/persistence-and-transactions.md`)
- `@Transactional` on service methods, not controllers or repositories. Class-level `@Transactional(readOnly = true)` on query services, plain `@Transactional` on write methods.
- Set `spring.jpa.open-in-view=false`. The default (true) holds a DB connection for the whole request and hides N+1 queries and lazy-loading mistakes until production load.
- Make every association `FetchType.LAZY` (`@ManyToOne` and `@OneToOne` default to EAGER - override them). Load what you need explicitly with `JOIN FETCH`, `@EntityGraph`, or a DTO projection.
- Beware `JOIN FETCH` on a collection combined with pagination: Hibernate paginates in memory. Page IDs first, then fetch, or use a projection.
- `@Transactional` is ignored on self-invocation (`this.other()`) and on non-public methods; call through another bean.
- Schema changes go through Flyway or Liquibase, never `ddl-auto=update` outside local dev.

### 8. Configuration
- Group settings in `@ConfigurationProperties` records annotated `@Validated`, registered with `@ConfigurationPropertiesScan`. Bad config then fails at startup instead of at 3 a.m. Reserve `@Value` for a single isolated value.
- No secrets in the repo. Read them from environment variables or a secret manager; keep one `application.yml` plus per-profile overrides, not copies per environment.
- Replace magic numbers and strings with named constants or properties.

### 9. Security (details in `references/testing-security-config.md`)
- Define a `SecurityFilterChain` bean with the lambda DSL. `WebSecurityConfigurerAdapter` is gone and `.and()` chaining is removed in Security 7.
- Deny by default: explicit `permitAll` for public routes, `anyRequest().authenticated()` last. Add `@EnableMethodSecurity` and `@PreAuthorize` for object-level rules; URL rules alone do not stop IDOR.
- Expose only needed Actuator endpoints (`management.endpoints.web.exposure.include=health,info`) and secure the rest.
- Hash passwords with `DelegatingPasswordEncoder` (BCrypt/Argon2). Never log credentials, tokens, or personal data.
- Validate and bound all input (sizes, page limits). Use parameterized queries only - never concatenate user input into JPQL/SQL.

### 10. Logging
- SLF4J with parameterized messages: `log.info("Order {} created", id)`, never string concatenation.
- Correct levels: ERROR for unexpected failures needing attention, WARN for recoverable anomalies, INFO for business milestones, DEBUG for diagnostics. No `System.out.println`, no `printStackTrace()`.
- Log an exception once, at the boundary that handles it, passing the exception as the last argument so the stack trace is kept.

### 11. Testing (details in `references/testing-security-config.md`)
- Unit-test services with plain JUnit 5 + Mockito (no Spring context).
- Slice tests for the web and data layers: `@WebMvcTest` for controllers and validation, `@DataJpaTest` for repositories.
- Real infrastructure via Testcontainers with `@ServiceConnection` rather than H2 when SQL behavior matters.
- Test the unhappy paths: invalid payload -> 400 with the expected ProblemDetail, missing resource -> 404, unauthorized -> 401/403.
- Tests are code too: no duplicated setup (use builders/fixtures), no commented-out tests, no `@Disabled` without a reason.

### 12. Dead and duplicate code (details in `references/code-hygiene.md`)
- Remove unused imports, private methods/fields, local variables, parameters, unreachable code, unused beans, endpoints, properties, and dependencies. Dead code is a maintenance and security liability: it still has to be read, compiled, and patched.
- Before deleting, check for framework use the tools cannot see (reflection, `@Bean`, `@EventListener`, `@Scheduled`, JPA callbacks, serialization, public endpoints used by other clients).
- Consolidate duplication when the same logic appears three times or the copies must change together. Do not force an abstraction onto code that merely looks alike but changes for different reasons.

## Boot 3 vs 4

Check the version before writing imports or test code. Facts verified against the Boot 4 migration material:

| Area | Boot 3.x | Boot 4.x |
|---|---|---|
| Web starter | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| Jackson | `com.fasterxml.jackson.*` (Jackson 2) | `tools.jackson.*` (Jackson 3); `Jackson2ObjectMapperBuilderCustomizer` -> `JsonMapperBuilderCustomizer` |
| Mock beans in tests | `@MockBean` (deprecated late 3.x) | `@MockitoBean` (`@MockBean` removed) |
| MockMvc test support | `spring-boot-test-autoconfigure` | split into `spring-boot-webmvc-test`; `MockMvcTester` available |
| Security config | `.and()` still compiles | Spring Security 7: lambda DSL only |
| Baseline | Java 17, Spring Framework 6 | Java 17, Spring Framework 7, Jakarta EE 11 |

Boot 4.1 (June 2026) is incremental. Prefer a currently supported Boot line, and when upgrading read the official migration guide; OpenRewrite has recipes that automate most of it. Never mix `fasterxml` and `tools.jackson` imports in one module unless you know why.

## Review output

When reviewing, report findings, don't just list rules. Use this structure:

```
## Summary
<2-3 sentences: overall health, the single biggest risk>

## Findings
| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 1 | High | OrderController.java:42 | Entity returned directly from API | Return OrderResponse record; map in service |

## Dead / duplicate code
<what was found, what is safe to remove, what needs confirmation>

## What's already good
<one or two items worth keeping>
```

Severity guide: **High** = correctness, security, data exposure, production performance. **Medium** = maintainability or reliability. **Low** = style or polish. Cite file and line when you have them. Order by severity. If a finding depends on code you did not see, say so instead of asserting it.

After a review, offer to apply the fixes; for large cleanups, do them in small, reviewable steps and keep behavior unchanged unless the user agrees to the change.

## Before you finish

Run through this list on anything you wrote or modified:
- [ ] Constructor injection, no field injection; no business logic in controllers
- [ ] No entity in any request/response; DTOs are records
- [ ] `@Valid` present on every `@RequestBody`; nested `@Valid` where needed
- [ ] Errors flow through the central ProblemDetail handler; no empty catch, no log-and-rethrow
- [ ] Public APIs have meaningful Javadoc; no commented-out code, no stale or restating comments
- [ ] No unused imports, variables, methods, or duplicated blocks left behind
- [ ] Associations LAZY, no N+1 introduced, `@Transactional` on services only
- [ ] No secrets, magic values, or sensitive data in logs
- [ ] Tests cover the happy path and at least one failure path
- [ ] If a shell is available: project compiles and tests pass (`./mvnw -q verify`)

## Reference files

Read the relevant file when the task touches that area; they hold the detail and code samples this file omits.

- `references/api-validation-errors.md` - request DTO and controller examples, validation groups, custom constraints, `@RestControllerAdvice` with ProblemDetail, status code table
- `references/code-hygiene.md` - Javadoc templates, comment audit, dead-code and duplicate-code detection with PMD/CPD, Checkstyle, SpotBugs, and `dependency:analyze`; safe-removal procedure
- `references/persistence-and-transactions.md` - fetch strategies, N+1 fixes, projections, pagination, transaction pitfalls, entity rules
- `references/testing-security-config.md` - test slices, Testcontainers, `SecurityFilterChain` example, configuration properties, Actuator hardening
