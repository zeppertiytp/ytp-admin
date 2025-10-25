# YTP Admin

Reusable Vaadin Flow components packaged as a Maven module with a companion
sample application.

## Project layout
- `components/` – library that exposes reusable Flow components, services, and
  frontend assets under the base package `com.youtopin.vaadin`
- `samples/` – Spring Boot demo that consumes the component library and
  demonstrates the UI patterns (see [`samples/README.md`](samples/README.md))
- `docs/` – architecture notes, component guides, and how-to material. Start with:
  - [`docs/components/README.md`](docs/components/README.md) – component catalogue and usage guides
  - [`docs/form-engine-reference.md`](docs/form-engine-reference.md) – annotation-driven form engine reference
  - [`docs/how-to.md`](docs/how-to.md) – practical workflows for extending the platform

## Prerequisites
- Java 17 or newer on the PATH
- Node.js 18+ (required by Vaadin for frontend builds)
- Maven 3.9+

## Build
Run the full reactor build (components + samples + tests):
```bash
mvn clean verify
```

Install just the reusable component library into your local Maven cache so other
projects can depend on it:
```bash
mvn -pl components clean install
```

> [!NOTE]
> The repository includes `.mvn/maven.config` with the `-am` flag so that when you
target a single module, Maven automatically builds the modules it depends on.
> For example, running `mvn -pl samples spring-boot:run` will now also build the
> `vaadin-components` module before launching the demo.

## Run the sample application
Start the Vaadin dev server from the sample module:
```bash
mvn -pl samples spring-boot:run
```
The app will be available at http://localhost:8080 (try `/`, `/login`,
`/design-system`, `/forms`, and `/wizard`). Login with **admin / admin**.

## Production bundle
Create an optimised frontend build and fat JAR for the sample showcase:
```bash
mvn -pl samples clean package -Pproduction
```
The resulting artifact lives at `samples/target/vaadin-samples-0.0.1-SNAPSHOT.jar`.

## Documentation & how-to
- Component-specific guidance lives under `docs/components/` and links back to
  source files and sample routes.
- The sample application structure and routes are described in
  [`samples/README.md`](samples/README.md).
- Use [`docs/how-to.md`](docs/how-to.md) for checklists when adding components,
  extending the form engine, or publishing new demos.

## Troubleshooting
- Use `mvn -X ...` for verbose logs if the build fails.
- Delete `node_modules` and `package-lock.json` under `samples/` if frontend
  tooling becomes inconsistent, then re-run the build.
- If you add new Vaadin components or dependencies, remember to update the BOM
  version in the root `pom.xml`.

