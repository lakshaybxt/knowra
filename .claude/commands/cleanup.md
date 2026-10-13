---
description: Find and safely remove dead code, duplicates and unused dependencies
---

Clean up this project. Work in this order and stop to report after each step:

1. Run `./mvnw dependency:analyze` and list unused declared dependencies. Before
   removing any, check whether it is a Spring Boot starter, runtime-only driver,
   or annotation processor. Do not remove those.
2. Run PMD (unused code rules) and CPD, and also search for classes, methods and
   config properties with no references. Treat anything used via Spring
   annotations (@Component, @Bean, @EventListener, @Scheduled, @ConfigurationProperties),
   reflection, or application.yaml as NOT dead.
3. For each finding, state the file, why you believe it is unused or duplicated,
   and your confidence. Show the list and wait for my approval.
4. After approval, make the changes in small commits, one concern per commit.
5. Run `./mvnw clean verify` after each batch. If tests fail, revert that batch.

Do not change behavior, public APIs, or database migrations.