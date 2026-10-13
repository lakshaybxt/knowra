---
name: generate
description: Scans the repository to infer the repo's actual coding conventions, then generates a concise "Coding Practices & Rules" document plus suggested Cursor rules under .cursor/rules. Use when the user asks to generate repo rules, scan for conventions, create coding standards, or run /generate.
---

# /generate — Repo Coding Practices & Rules

Stay generic: infer from **this** tree. Do not reuse folder names, stacks, or response shapes from the examples below or from a previous repo.

## What to produce

1. **Repository Coding Practices (human doc)** at repo-root `CODING_PRACTICES.md` (overwrite if regenerating).
2. **Cursor rules**: at least `.cursor/rules/coding-standards.md` encoding the same practices.

Prefer evidence over "what's ideal." If a surface does not exist, skip it.

## How to scan the repo

### 1) Identify the major surfaces

Discover apps/services from top-level dirs and build files (`package.json`, `pom.xml`, `build.gradle`, `go.mod`, Docker/CI, etc.). Typical kinds:

- Frontend app(s)
- Backend service(s) or API(s)
- Workers/consumers, gateways, registries
- Shared config (`.gitignore`, CI, lint/format)

Do not assume a two-folder layout or specific names.

### 2) Infer conventions from evidence

Look for whatever applies:

- **Languages & module system**: e.g. Java/Maven/Gradle + package names; Node ESM vs CJS / `"type": "module"`; Python modules. Record versions from build files.
- **Tooling**: linters, formatters, TypeScript, test runners, Maven/Gradle plugins, build scripts/commands.
- **Folder structure**: as it exists (e.g. `controller`/`service`/`repos`, or `src/routes`/`models`/`services`, or `pages`/`features`).
- **API patterns** (REST, GraphQL, RPC — only what is present):
  - Path prefixes and resource naming
  - Auth (JWT, filters, headers, cookies, public vs protected paths)
  - Response shapes (record real JSON/DTO fields, not a template envelope)
  - Error handling (status codes, error DTO, `@ControllerAdvice`, middleware)
- **UI patterns** (only if a frontend exists): API base URL/env, storage keys, token handling, state library, routing.
- **Config & secrets**: `application.yaml`/`yml`/`properties`, `.env`, hardcoded hosts/ports.
- **Style**: indent, quotes, semicolons, naming (types, files, constants) from real files.

### 3) Record both "current rules" and "fixme rules"

If inconsistent, document:

- **Current observed behavior**
- **Proposed standard** (going forward)
- **Low-risk migration steps** (optional)

## Output format (must follow)

### A) `CODING_PRACTICES.md`

Use this template. Drop a conventions subsection if that surface is absent.

- **Project overview**: 2–4 bullets about architecture.
- **Language & tooling**: key packages/commands (scripts).
- **Repo conventions**:
  - **Directory structure**
  - **Naming**
  - **API design**
  - **Error handling**
  - **Validation**
  - **Security**
  - **Frontend data fetching** (omit if no UI)
  - **Config & env**
- **Inconsistencies found**: list with recommended resolution.

### B) `.cursor/rules/coding-standards.md`

Must:

- Be concise and enforceable (Do/Don't).
- Include **this repo's** response shapes, path prefixes, naming, and patterns.
- Use Cursor rule frontmatter (`description`, `alwaysApply` or `globs` as appropriate).