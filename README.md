# StudioDroid

**An Android re-platforming research project inspired by Android Studio Desktop.**

StudioDroid aims to bring an Android Studio-like development environment directly to Android phones. The project investigates which parts of the Android Studio and IntelliJ codebases can be reused, adapted, or replaced with Android-native components.

> **Unofficial project:** StudioDroid is not affiliated with, endorsed by, or an official product of Google or JetBrains. Android Studio and IntelliJ are trademarks of their respective owners.

## Product goal

Build one Android IDE with two automatic performance profiles:

| Device memory | Default profile | Policy |
| --- | --- | --- |
| 6 GB RAM or less | **Low-memory** | Reduce memory pressure and background concurrency while preserving user-facing features. |
| More than 6 GB RAM | **Standard** | Keep standard behavior; do not apply the project's low-memory reductions. |

The 6 GB threshold is the project's default policy, not a claim that physical RAM alone determines performance. Android memory class, available memory, and low-RAM device signals will also be considered for safe runtime decisions.

### Low-memory profile

The low-memory profile may:
- Limit background indexing concurrency and process large projects incrementally.
- Bound in-memory caches and release inactive editor resources.
- Limit build parallelism based on available memory.
- Defer non-essential background work while the IDE is inactive.
- Keep core editor behavior, syntax highlighting, navigation, and code assistance available where supported.

It must **not** silently remove core IDE features or lower source-code correctness.

### Standard profile

Devices above the 6 GB threshold use standard component defaults and normal concurrency. The low-memory profile's special restrictions are not applied.

## Reference device

Initial physical-device testing will use:
- Xiaomi Redmi 14C
- Android 16 (as reported by the project maintainer)
- 6 GB RAM

The reference device is a test target, not the minimum supported device specification.

## Technical direction

The project investigates:
- Android Studio source components and their licensing.
- IntelliJ Platform dependencies and desktop-only assumptions.
- An Android-native host application and lifecycle integration.
- Editor, project model, Gradle, language tooling, and debugging compatibility.
- ARM64 availability of required runtimes and native dependencies.

Android Studio's source build documentation identifies the Android plugin, IntelliJ IDE base, and shared libraries as major source areas. It also documents a multi-repository checkout and Bazel-based build workflow. This does **not** mean the desktop IDE can run on Android unchanged.

## Current status

**Stage: Phase 0 — preliminary source audit.** The audit identifies the desktop UI, runtime, and host toolchain as major porting boundaries. No desktop source has been copied, no Android module has passed an ART/ARM64 compatibility test, and no APK is available yet.

Read the [Phase 0 feasibility audit](docs/PHASE-0-AUDIT.md) before proposing source reuse.

## Documentation

- [Phase 0 source and feasibility audit](docs/PHASE-0-AUDIT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Memory profiles](docs/MEMORY-PROFILES.md)
- [Roadmap](docs/ROADMAP.md)
- [Contribution guide](CONTRIBUTING.md)
- [AI agent instructions](AGENTS.md)
- [Third-party source and license audit](THIRD_PARTY_NOTICES.md)

## Development approach

The maintainer's only development device is an Android phone. Source control and documentation are hosted on GitHub. Builds may use GitHub Actions or another reproducible remote CI environment when local Android builds exceed the phone's practical limits. Physical install and usability testing must still be performed on real Android devices.

## License

The license for original StudioDroid code is **planned to be Apache-2.0**, subject to final confirmation. Every upstream component must retain its own license and notices. Do not assume that all Android Studio or IntelliJ components share one license; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
