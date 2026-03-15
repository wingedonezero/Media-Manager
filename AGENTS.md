# AGENTS.md

## Purpose

- tinyMediaManager is a Java desktop app (Swing + FlatLaf) with both GUI and CLI entry points, centered on managing
  movie/TV metadata, artwork, renaming, and exports for Kodi/Plex/Emby/Jellyfin.
- Start reading at `src/main/java/org/tinymediamanager/TinyMediaManager.java` (`startup()`, `loadModules()`,
  `loadPlugins()`, `loadServices()`) to understand runtime order and side effects.

## Architecture That Matters

- Core boundaries are stable: `core` (business/data), `ui` (Swing), `scraper` (providers), `thirdparty` (external
  integrations), `cli` (headless commands).
- Modules are explicit singletons registered via `TmmModuleManager`: `MovieModuleManager` and `TvShowModuleManager` (
  `TmmModuleManager.java`, `MovieModuleManager.java`, `TvShowModuleManager.java`).
- Each media module persists to H2 MVStore (`movies.db`, `tvshows.db`) and keeps JSON payloads per entity map; startup
  includes corruption recovery and backup restore logic.
- Internal REST API contexts are mounted under `/api/*` via `TmmHttpServer` (default `command`, plus `movie` and
  `tvshow` contexts from module startup).
- Scraper providers are centrally loaded in `MediaProviders.loadMediaProviders()` with optional addon discovery through
  `ServiceLoader<IAddonProvider>`.

## Concurrency and Events (project-specific)

- Use `TmmTaskManager` queues instead of ad-hoc executors: main tasks are serialized, downloads are bounded, image/cache
  work has dedicated pools.
- Use `TmmThreadPool`/`TmmTaskManager` for background work and keep Swing UI updates on EDT (
  `SwingUtilities.invokeLater` pattern in `TinyMediaManager.java`).
- Event aggregation is debounced (~250ms) in `EventBus.publishEvent()`; listeners are topic-based (`movies`,
  `movieSets`, `tvShows`, etc.).

## Data and Settings Patterns

- Observable models inherit from `AbstractModelObject` (SwingPropertyChangeSupport) and are expected to fire property
  changes from setters.
- Settings classes inherit `AbstractSettings`, serialize to JSON, preserve unknown fields via `@JsonAnySetter`, and use
  dirty-flag based persistence.
- User-visible strings must come from `TmmResourceBundle` + `src/main/resources/messages*.properties` (missing keys
  render as `???`).

## Build, Test, and QA Workflows

- Full package build (as documented): `mvn -P dist clean package` (requires GitHub Packages credentials in Maven
  settings).
- Unit tests are skipped by default (`pom.xml` has `<skipTests>true</skipTests>`); run explicitly with
  `mvn -DskipTests=false -DskipITests=true clean test`.
- Integration tests are separate (`src/integration-test/java`, failsafe patterns `*IT*.java`) and gated by
  `-DskipITests=false`.
- CI examples in `.gitlab-ci.yml` use `clean test` on JDK 17/21/25 and QA profile `-Pqa` for verify/sonar tooling.

## Debugging and Runtime Knobs

- Primary logs: `logs/tmm.log`, trace sessions `logs/trace-*.log`, startup boot log `logs/startup.log` (
  `src/main/resources/logback.xml`).
- Useful JVM properties: `tmm.contentfolder`, `tmm.datafolder`, `tmm.logfolder`, `tmm.consoleloglevel`, `tmm.debug`,
  `tmm.mvstore.buffersize` (`Globals.java`, `TmmLoggingUtils.java`, module managers).
- Startup always performs backups and may auto-recover databases from `backup/data.*.zip`; preserve this behavior when
  touching persistence.

## Integrations and Extension Points

- External binary tools (ffmpeg, yt-dlp, deno) are declared in `external-tools.json` and managed by
  `thirdparty/ExternalTools.java`.
- UPnP and Kodi RPC are optional services wired in `TinyMediaManager.loadServices()` (`Upnp`, `KodiRPC`).
- CLI path is `TinyMediaManagerCLI` (picocli), including `--update` and `--start-api` modes.

## Code Conventions to Follow Here

- Keep existing stable behavior; avoid refactors unless asked (see `.github/copilot-instructions.md`).
- Java style in this repo: 2-space indentation and Apache 2.0 header in every Java file (`code_formatter.xml`, existing
  source files).
- Preserve module separation (movie vs tvshow) and avoid cross-module coupling unless there is an existing shared
  abstraction.
- If you touch web UI assets under `src/main/webapp`, run the local frontend checks/build commands documented in
  `.github/copilot-instructions.md` when applicable.

