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

StudioDroid is being developed as **one Android-native IDE application**. Internal engineering tasks are split for implementation and verification, but they are not separate products or user-facing releases. The current app foundation includes a Compose code editor, document open/save, project-folder browsing, multi-file tabs, and workspace file operations. The latest change adds create, rename, and delete actions for project files and folders through Android's Storage Access Framework.

The editor and workspace foundation has not yet completed physical-device verification as a combined workflow. The initial editor-only build was manually tested on Redmi 14C / Android 16; the newer workspace and file-management behaviors still require device testing. The current source successfully built a debug APK in [GitHub Actions run #15](https://github.com/yyouki/studio-droid/actions/runs/41406161465). The build includes project-root restoration when a persisted Storage Access Framework grant remains valid. This is an early app foundation, not yet a complete Android IDE or an Android Studio port.

The wider product still needs project restoration, robust editing workflows, language services, Gradle/JDK build execution, diagnostics, Git, and resource policies for LOW_MEMORY devices. The Android Studio / IntelliJ source audit remains useful for deciding which desktop features can be reused and which require Android-native implementations.

## Documentation

- [Claude AI review handoff](docs/CLAUDE-HANDOFF.md)
- [Redmi 14C device test report](docs/PHASE-1-DEVICE-TEST.md)
- [Workspace explorer and tab design](docs/WORKSPACE-DESIGN.md)
- [Workspace APK device test checklist](docs/PHASE-2-WORKSPACE-TEST.md)
- [Phase 0 source and feasibility audit](docs/PHASE-0-AUDIT.md)
- [Editor and toolchain portability review](docs/EDITOR-AND-TOOLCHAIN-PORTABILITY.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Memory profiles](docs/MEMORY-PROFILES.md)
- [Product development plan](docs/ROADMAP.md)
- [Contribution guide](CONTRIBUTING.md)
- [AI agent instructions](AGENTS.md)
- [Third-party source and license audit](THIRD_PARTY_NOTICES.md)

## Development approach

The maintainer's only development device is an Android phone. Source control and documentation are hosted on GitHub. Builds may use GitHub Actions or another reproducible remote CI environment when local Android builds exceed the phone's practical limits. Physical install and usability testing must still be performed on real Android devices.

## License

The license for original StudioDroid code is **planned to be Apache-2.0**, subject to final confirmation. Every upstream component must retain its own license and notices. Do not assume that all Android Studio or IntelliJ components share one license; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
