# AGENT.md — Vaadin Flow Admin Panel (v0.0.1)

_Last updated: 2025-09-17 17:40:34_

> **Note:** This AGENT.md was generated without access to the repo archive. Replace the placeholder sections (tree, module, versions) after syncing.

## Purpose
This document equips **ChatGPT Codex / Code Interpreter style agents** to work on the **Vaadin Flow Admin Panel** repo autonomously and safely. It captures conventions, architecture, build/run steps, and guardrails.

## Fast facts (TL;DR)
- **Stack:** Java with (likely) Spring Boot, Vaadin Flow (Maven/Gradle)
- **Module:** com.example:adminpanel
- **UI goals:** Modern flat design, full **light/dark** theme, **responsive** layouts, reusable components, strong **i18n** with **RTL (Farsi)** support and **Vazirmatn** font.
- **Auth:** local demo login (`admin` / `admin`); production via OAuth (hooks/stubs).
- **Data:** No DB by default; use **MongoDB** if persistence is added.
- **Versioning:** Keep `version.txt` in sync. Current version: **0.0.1**.

## Repo layout (placeholder)
```
<repo tree unavailable — populate after sync>
```

## How to build & run
### Prereqs
- JDK 17+ (LTS recommended)
- Node & npm (Vaadin frontend build)
- Maven or Gradle

### Commands
#### Maven
- `mvn -Pproduction clean package`
- `mvn spring-boot:run` (if Spring Boot)
- `mvn -DskipTests spring-boot:run`

#### Gradle
- `./gradlew clean build`
- `./gradlew bootRun` (if Spring Boot)

Dev mode (hot reload): run the app and edit Java/TS; Vaadin handles live reload.

### Where it runs
- Default: `http://localhost:8080`
- Demo login: **admin / admin**

## Architecture & policies
- Views in `.../view`, components in `.../component`, services in `.../service`, security in `.../security`.
- Theming in `frontend/themes/<theme-name>`; tokens for primary/secondary/info/success/warning/danger.
- i18n bundles in `src/main/resources/messages*.properties`, **fa-IR** default with RTL.
- Prefer DI, composition over inheritance, accessibility first, localized validation.

## Reusable components (starter kit)
- SmartForm (JSON‑driven forms)
- FileUpload (single/multi with size & remove)
- GeoSelector (location input + pluggable map)
- DataGrid (lazy pageable grid)
- DialogSheet (full‑height modal)
- Toast helpers

## Contributing workflow (for agents)
1. Add `docs/ADR-<yyyyMMdd>-<slug>.md` for any non‑trivial change.
2. Build and run locally; ensure the UI loads.
3. Add/Update tests (JUnit, TestBench/Karibu).
4. Verify i18n/RTL and accessibility.
5. Bump `version.txt`; update `CHANGELOG.md`.
6. Zip and hand off when requested.

## Guardrails
- No secrets/PII; follow official docs first (Vaadin, Spring, MongoDB).
- Keep public APIs stable; write migration notes if breaking.
- Methods < 40 LOC where reasonable; refactor aggressively.

## TODOs
- OAuth wiring + config profiles.
- Dark-mode auto toggle + token audit.
- RTL snapshot tests.
- Map provider integration for GeoSelector.
- MongoDB sample profile + docker-compose.
