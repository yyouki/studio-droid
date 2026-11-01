# Architecture

## 1. Objective

StudioDroid is an Android re-platforming effort based on research into Android Studio Desktop and the IntelliJ Platform. The goal is to preserve as much useful IDE behavior and compatible upstream logic as possible while making the application installable and usable on Android phones.

This is not a promise that the desktop distribution can be copied into an APK unchanged.

The current feasibility findings are recorded in [PHASE-0-AUDIT.md](PHASE-0-AUDIT.md). Phase 0 remains open until module-level dependency and licensing checks are complete.

## 2. Upstream source map

Official Android Studio source-build notes identify these major areas:
- `tools/adt/idea`: Android plugin source code.
- `tools/idea`: IntelliJ IDE base.
- `tools/base`: shared libraries and Android developer tooling.

The Android Studio source checkout is a multi-repository Repo manifest. The source build uses Bazel and host-specific prebuilts. It is not a single Gradle Android app project.

Preliminary audit snapshots (moving upstream branches; not a frozen product baseline):
- Manifest: `platform/manifest`, branch `studio-main`, blob `9df3d97fa144d035edc63338f68b2f4180fed70e`.
- Android plugin: `platform/tools/adt/idea`, branch `mirror-goog-studio-main`, commit `224edb8a5a99071fc2fb02bb7a4f80021723ccf6`.
- Shared tools: `platform/tools/base`, branch `mirror-goog-studio-main`, commit `45628254f0f8a9bb3755d73f3d30ca78fbb2153d`.
- Exact current `tools/idea` SHA still needs to be captured by syncing the manifest.

References:
- [Android Studio build instructions](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main/studio.md)
- [Android Studio source manifest](https://android.googlesource.com/platform/manifest/+/refs/heads/studio-main/default.xml)
- [Android plugin source](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main)
- [Shared Android tooling source](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main)

## 3. Preliminary compatibility boundary

The project must separate reusable IDE logic from desktop-specific integration.

| Area | Initial direction | Reason |
| --- | --- | --- |
| Android Studio desktop shell and windowing | Replace | Android needs an Activity/lifecycle and Android-native navigation. |
| IntelliJ Swing/AWT UI | Replace | IntelliJ-based IDE UIs primarily use Swing/AWT; the desktop component hierarchy is not an Android UI. |
| Editor view component | Replace or deeply adapt | The editor's text model may be useful, but the desktop view is not assumed to run on Android. |
| PSI, parsers, indexes, inspections | Investigate and adapt | Valuable language features, but dependencies on IntelliJ services, VFS, threading, and JVM APIs must be mapped. |
| Android plugin domain logic | Adapt selectively | Source modules are IntelliJ plugin code, not stand-alone Android libraries. |
| `tools/base` libraries and SDK tooling | Candidate reuse | Some modules are shared by IDE, AGP, and SDK tools, but need module-level runtime tests. |
| Gradle / Android Gradle Plugin | Investigate | Must prove a compatible runtime, native tools, SDK paths, and process model on Android ARM64. |
| ADB / device communication / JDWP | Candidate adaptation | Protocol logic may be separable; Android permissions and process behavior require new integration. |
| Debugger UI | Adapt | Protocol layer may be reusable; desktop UI integration must be replaced. |
| JCEF browser UI | Replace | JCEF is embedded in Swing applications; use Android WebView for suitable preview use cases. |
| Desktop plugin compatibility | Defer | Plugins relying on IntelliJ desktop UI and host APIs cannot be presumed compatible. |
| Android Emulator | Exclude from first phone release | Start with physical Android device testing and remote testing where needed. |
| Bazel and upstream host build tools | Remote/replace for project builds | Current manifest host prebuilts target desktop OS/architectures, not an Android ARM64 app host. |

All reuse classifications are provisional until source dependencies, licenses, and Android ARM64 runtime behavior have been checked.

## 4. Platform boundary

### Android host

Responsibilities:
- Application lifecycle, activities, services, permissions, and process management.
- Android Storage Access Framework integration for project files.
- Android-native navigation, windows, dialogs, keyboard handling, and touch interactions.
- Memory-profile detection and runtime policy.
- Safe process supervision for build and language tools.

Candidate implementation: Kotlin and Android Jetpack components. The exact UI framework and editor component will be selected after a small device prototype.

### IDE compatibility layer

Responsibilities:
- Isolate upstream APIs behind interfaces.
- Adapt project model, workspace, settings, diagnostics, and editor services.
- Replace unsupported desktop integrations instead of allowing them to leak into Android code.
- Keep upstream-specific code and StudioDroid-owned code clearly separated.
- Allow a candidate upstream service to be replaced if ART/API compatibility fails.

### Editor and language services

Responsibilities:
- Editing, tabs, syntax highlighting, navigation, diagnostics, and completion.
- Language services run only when their runtime and dependencies are compatible with Android or an explicitly supported execution environment.
- Expensive services must support cancellation, incremental work, and bounded resource use.
- The first vertical slice must prove file open/edit/save before advanced language intelligence is attempted.

### Build and toolchain layer

Responsibilities:
- Gradle project import and task execution.
- Android SDK and build-tools discovery.
- Kotlin and Java compilation support.
- Toolchain installation and version management.
- Clear reporting when a required tool is unavailable or incompatible.

The initial implementation must prove that a small Android project can be opened and inspected before promising full project builds.

## 5. Porting strategy

Work component-by-component:

1. Inventory source modules, licenses, native libraries, JDK assumptions, desktop APIs, and build requirements.
2. Classify each component as reusable, adaptable, replaceable, or unsupported.
3. Build a minimal Android host and prove its lifecycle, storage, and process behavior.
4. Integrate one editor capability at a time.
5. Add project import and build tooling only after ARM64 and Android-runtime compatibility are demonstrated.
6. Add debugging and advanced IDE integrations after the core workflow is stable.

Do not copy the full Android Studio or IntelliJ source tree into this repository as a first step. Keep upstream references pinned to exact revisions and preserve their notices.

## 6. Adaptive memory design

Two default profiles are required:

- **Low-memory:** total physical RAM is at or below 6 GiB.
- **Standard:** total physical RAM is above 6 GiB.

The threshold is a product policy. Detection should use Android system information such as `ActivityManager.MemoryInfo.totalMem`; use `ActivityManager.isLowRamDevice()` and memory-class information as supporting signals and fallbacks. Temporary available-memory pressure may pause work safely, but must not permanently switch a standard device into the low-memory profile.

Low-memory mode must optimize resource usage without silently removing supported editor or language features. Standard mode must not receive StudioDroid's special low-memory reductions.

See [MEMORY-PROFILES.md](MEMORY-PROFILES.md).

## 7. Build and test architecture

The maintainer's reference device is an Android phone, so the repository should support reproducible CI builds. GitHub Actions can run compatible build jobs and publish artifacts, but CI success does not prove that an APK works on a physical phone.

Required validation:
- Static checks and unit tests in CI.
- APK install and launch on a physical Android device.
- Project open/edit/save workflow.
- Memory and responsiveness measurements on a 6 GB-or-lower device.
- Standard-profile regression tests on a device above 6 GB when available.

## 8. Non-goals for the first milestone

- Full compatibility with every Android Studio Desktop plugin.
- Running the desktop Swing/AWT UI unchanged inside Android.
- Shipping every Android SDK platform and emulator image inside the APK.
- Claiming support for a compiler, debugger, or language server before it has been tested on Android ARM64.
- Removing editor or language features solely to make the low-memory profile appear faster.

## 9. Open technical questions

- Which IntelliJ Platform services can run on Android Runtime without major modification?
- Which UI and windowing services need Android-native replacements?
- Which upstream modules can legally and technically be redistributed?
- Which Android SDK and Gradle components can be obtained and executed on-device?
- What process model works reliably under Android background-execution limits?
- Which editor component provides the best mobile editing experience while preserving enough language-service integration?
