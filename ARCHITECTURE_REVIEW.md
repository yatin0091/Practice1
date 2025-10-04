# Architecture Review

## Overall Rating
**Score: 6 / 10** — The project follows several of Google's recommended Android architecture practices (unidirectional data flow, dependency injection, Jetpack libraries), but important gaps remain around layering, state modeling, and configuration management that keep it from being production-ready.

## Strengths
- **Dependency injection is in place.** The app uses Hilt modules to provide networking dependencies and bind the repository interface to its implementation, which keeps construction logic centralized and testable.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/DataModule.kt†L9-L17】【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/network/NetworkModule.kt†L16-L76】
- **UI observes a reactive data source.** `PhotoVm` exposes a Paging `Flow` that the Compose UI collects, giving you a single source of truth for the list and aligning with the recommended unidirectional data flow pattern.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/ui/photo/PhotoVm.kt†L13-L15】【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/ui/photo/PhotoScreen.kt†L37-L120】
- **Screens handle loading and error states.** The Compose layer reacts to `LoadState` to show progress and retry affordances, demonstrating awareness of resilient UI requirements.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/ui/photo/PhotoScreen.kt†L37-L120】

## Gaps vs. Recommended Architecture
- **No domain layer or UI-focused models.** The ViewModel exposes raw paging data straight from the repository, tying the UI to remote DTOs (`Photo` mirrors the network schema). Google recommends introducing a domain layer and UI models to decouple presentation from transport and to hide backend changes from the UI.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/ui/photo/PhotoVm.kt†L13-L15】【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/Photo.kt†L7-L65】
- **UI state is not explicitly modeled.** Beyond the paging list, there is no sealed UI state (e.g., success, empty, error) surfaced from the ViewModel. The composable inspects Paging `LoadState` directly, which increases coupling and makes alternative UI (or tests) harder to implement.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/ui/photo/PhotoScreen.kt†L37-L120】
- **Configuration data is hard-coded.** The Unsplash API key lives in source code, violating security best practices and preventing per-build configuration. Recommended practice is to inject it via build config or encrypted storage.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/network/NetworkModule.kt†L32-L35】
- **Repository is thin and lacks caching.** The repository merely forwards to a network paging source with no local database or offline cache, so the app has no resilience to network loss and cannot satisfy "single source of truth" guidance.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/PhotoRepo.kt†L10-L26】【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/PhotoPagingSource.kt†L20-L34】
- **Network layer logs full payloads in all builds.** The interceptor is pinned to `BODY` level, which is discouraged for release builds and may leak PII or slow the app.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/data/network/NetworkModule.kt†L37-L55】
- **Leftover template code.** `MainActivity` still contains the generated `Greeting` composable that is unused, hinting at incomplete cleanup and potentially confusing future contributors.【F:app/src/main/java/com/software/pandit/lyftlaptopinterview/ui/MainActivity.kt†L34-L37】
- **Test coverage is absent.** There are no unit or instrumentation tests verifying repository, ViewModel, or UI behavior, which is expected for high-quality submissions.

## Recommendations
1. Introduce a domain layer (use cases or domain models) that maps from network DTOs to UI-friendly data classes; expose immutable `UiState` from the ViewModel.
2. Secure configuration secrets by moving the API key to build config fields or remote config, and inject via `@Named` qualifiers rather than hard-coding.
3. Add a persistence layer (Room or other) and make the repository return a cached flow to satisfy the single-source-of-truth recommendation.
4. Provide environment-aware logging (e.g., conditional on `BuildConfig.DEBUG`).
5. Remove template leftovers and add targeted unit tests (ViewModel, repository) plus UI tests to validate paging/error flows.

Addressing these gaps would align the project much more closely with Google's modern Android architecture guidance and improve maintainability for interview reviewers.
