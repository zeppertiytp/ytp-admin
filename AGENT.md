# AGENT.md — Vaadin Flow Admin Panel (v4.6.14)

_Last updated: 2025-10-24 10:15:00Z_

## Purpose
This document equips ChatGPT-style agents to work safely on the Vaadin Flow Admin Panel monorepo. It outlines the module layout, build commands, documentation expectations, and contribution guardrails.

## Repository layout
```
.
├── components/                # Reusable Flow components and form engine
│   └── src/main/java/com/youtopin/vaadin/
│       ├── component/         # UI components (cards, notifications, grids, wizards)
│       ├── formengine/        # Annotation-driven form generator runtime
│       ├── form/              # JSON form renderer helpers
│       ├── navigation/        # Menu services
│       └── data/              # Pagination utilities
├── samples/                   # Spring Boot showcase consuming the component library
│   ├── src/main/java/com/youtopin/vaadin/samples/
│   └── frontend/              # Theme, routes, and shared frontend assets
├── docs/                      # Architecture notes, component how-tos, ADRs
├── CHANGELOG.md               # Keep in sync with `version.txt`
├── version.txt                # Current release tag and packaging timestamp
└── README.md                  # High-level usage guide
```

## Build & run
- **Full reactor build:** `mvn clean verify`
- **Library only:** `mvn -pl components clean install`
- **Run samples in dev mode:** `mvn -pl samples spring-boot:run`
- **Production bundle:** `mvn -pl samples clean package -Pproduction`

The Vaadin dev server listens on http://localhost:8080. Demo login uses `admin` / `admin`.

## Coding standards & docs
- Keep methods under ~40 LOC. Prefer composition and dependency injection.
- Update or create documentation under `docs/` whenever you touch a component, sample, or engine feature.
- Add ADRs under `docs/ADR-<yyyyMMdd>-<slug>.md` for architectural decisions.
- Maintain RTL, accessibility, and i18n support (fa-IR is the default locale).

## Contribution checklist
1. Run the relevant Maven build before committing.
2. Update `CHANGELOG.md` and `version.txt` when behaviour or documentation changes warrant a new release.
3. Sync README sections if new docs, samples, or commands are introduced.
4. Verify screenshots for UI changes when possible.

## Support contacts (for human collaborators)
- **Product design:** `design@youtopin.com`
- **Platform engineering:** `platform@youtopin.com`
- **Security reviews:** `security@youtopin.com`

