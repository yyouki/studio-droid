# Phase 0 — Android Studio source and port feasibility audit

**Audit date:** 2026-11-02  
**Status:** Preliminary source-tree audit. No source has been copied into StudioDroid, and no component has yet passed an Android ARM64 runtime test.

## Executive finding

A literal port of the complete Android Studio Desktop distribution into an Android APK is not a realistic first implementation. The major blockers are the desktop UI framework, assumptions about a desktop JVM and host operating system, and a build/source tree configured around desktop host tools.

A credible route is a **selective re-platforming**:
1. Build a native Android host and interaction model.
2. Reuse only separable platform-independent services and libraries after runtime and license checks.
3. Replace desktop UI and host integration with Android implementations.
4. Add language, project, build, and debugger capabilities one subsystem at a time.

This preserves the user's objective—an Android IDE based on Android Studio's capabilities—without claiming that the desktop IDE itself can run unchanged.

## 1. Source layout and snapshots examined

The Android Studio source documentation identifies three important source areas:
- `tools/adt/idea`: Android plugin source code.
- `tools/idea`: IntelliJ IDE base.
- `tools/base`: shared libraries and Android developer tooling.

The `tools/adt/idea` repository describes itself as the Android plugin source for the IntelliJ Platform and the main part of Android Studio. The `tools/base` repository describes common libraries shared by the IDE, Android Gradle Plugin, and SDK components.

### Snapshot references

The source tree is actively updated. These SHAs are the evidence snapshots used for this preliminary audit, not a frozen build baseline:

| Source | Snapshot / reference | What it establishes |
| --- | --- | --- |
| Android Studio manifest | [studio-main/default.xml](https://android.googlesource.com/platform/manifest/+/refs/heads/studio-main/default.xml), blob `9df3d97fa144d035edc63338f68b2f4180fed70e` | Declares the multi-repository checkout and its default branch. |
| Android plugin | [tools/adt/idea](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main), commit `224edb8a5a99071fc2fb02bb7a4f80021723ccf6` | Shows the current plugin module tree and its IntelliJ Platform relationship. |
| Shared tools | [tools/base](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main), commit `45628254f0f8a9bb3755d73f3d30ca78fbb2153d` | Shows common tooling, build system, SDK utilities, lint, parsers, and profiler modules. |
| IntelliJ IDE base | [tools/idea](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main) | Confirms the IDE base is IntelliJ IDEA Community-derived. The currently referenced manifest uses the moving `mirror-goog-studio-main` branch; its exact project SHA still needs to be captured by syncing the manifest. |

Official build instructions say that Android Studio source builds use a checked-in Bazel executable and build targets from the source checkout. The manifest also aggregates many projects through the Repo tool; this is not a single Gradle Android app checkout.

## 2. Compatibility matrix

The classifications below are engineering hypotheses based on source organization and official platform documentation. "Candidate reuse" is not a claim that the component has already been built or tested on Android.

| Component / area | Preliminary classification | Reason and next test |
| --- | --- | --- |
| Android Studio product UI and application shell | **Replace** | The desktop shell and window lifecycle are not an Android Activity. Build an Android-native host. |
| IntelliJ Swing/AWT UI, tool windows, dialogs, popup system | **Replace / unsupported unchanged** | JetBrains documents that IntelliJ-based IDE UIs primarily use Swing and AWT. Recreate mobile navigation, tabs, panels, dialogs, and touch interactions using Android UI. |
| IntelliJ editor UI component | **Replace or deeply adapt** | The editor is a major part of the platform UI. The editor's text model and algorithms may be useful, but the desktop view component is not assumed to run on Android. Prototype editor behavior independently. |
| IntelliJ PSI, parsers, indexes, inspections, refactoring logic | **Investigate / adapt** | These are valuable language features, but their dependency graph reaches IntelliJ services, extensions, threading, VFS, and JVM APIs. Identify small leaf libraries and run Android/ART compatibility tests before reuse. |
| Android plugin project model and Android-specific logic | **Adapt selectively** | Modules are built as IntelliJ plugins and use platform APIs. Reuse domain logic only after decoupling and dependency/license checks. |
| Android plugin UI modules | **Replace / adapt** | UI modules depend on IntelliJ UI services. The current source tree includes some Compose-named modules, but their presence alone does not make them stand-alone Android UI libraries. |
| `tools/base` common libraries, parsers, SDK models, manifest merger, lint | **Candidate reuse** | The repo contains libraries shared by IDE, AGP, and SDK tooling. Audit each module's dependencies, filesystem assumptions, process calls, native code, and license. |
| Android Gradle Plugin / Gradle | **Investigate; do not assume on-device support** | The build tooling is separate from the desktop IDE, but needs a compatible Java runtime, Android-compatible native tools, writable SDK paths, and process support. Prove with a minimal Gradle task on Android ARM64. |
| ADB / device communication / JDWP protocols | **Candidate adaptation** | Protocol and client components may be separable. Android's own USB/device permissions and process model need dedicated implementation and testing. |
| Debugger UI and IntelliJ debug integration | **Adapt** | Protocol-level functionality may be reusable; desktop debugger views and IntelliJ integration need a new Android front end. |
| JCEF embedded browser | **Replace** | IntelliJ documents JCEF as a Chromium browser embedded in Swing applications. Use Android WebView for suitable preview use cases; verify security and local-server boundaries. |
| Desktop plugin compatibility | **Defer / not supported initially** | Plugins that expect IntelliJ's desktop UI, filesystem, or host APIs will not automatically run. A mobile-specific extension contract is a later design decision. |
| Android Emulator and desktop virtualization | **Exclude from first phone release** | The existing host setup is not an Android-phone emulator environment. Start with physical-device deployment and optional remote preview/testing. |
| Bazel / host build tools | **Replace or run remotely for project builds** | The current source manifest lists host prebuilts for Darwin/Linux/Windows x86_64; it does not list an Android ARM64 host toolchain. An Android-native build port would be separate work. |

## 3. Confirmed platform constraints

### 3.1 UI framework mismatch

JetBrains' IntelliJ Platform documentation says IntelliJ-based IDEs primarily use Swing and AWT. Its UI SDK documentation identifies the standard platform UI framework as Swing. Android's application UI model is based on Android framework components, so the desktop UI cannot be treated as a drop-in Android UI.

**Decision:** create a new Android-native presentation layer. Do not attempt to run the original IDE window hierarchy unchanged.

### 3.2 Runtime mismatch

Android apps run on ART, which executes DEX bytecode. The IntelliJ Platform is documented as a JVM-based application host. That description alone does not prove compatibility with Android's ART or Android API surface.

**Decision:** treat each Java/Kotlin library as unverified until it builds against the chosen runtime and passes Android device tests. A desktop JVM binary or JAR is not automatically Android-compatible.

### 3.3 Host toolchain mismatch

The checked-in Android Studio source manifest declares prebuilt Bazel, Clang, Python, and GCC host tools for desktop operating systems/architectures, including x86_64 host entries. Google's Android Studio installation page currently lists 8 GB minimum RAM for the desktop IDE alone and says Linux ARM-based machines are not supported.

**Decision:** the full upstream source build is not our phone build pipeline. Use GitHub Actions or another remote CI host for source analysis/builds where required; build the actual Android app through an Android-compatible workflow and validate APKs on the Redmi 14C.

### 3.4 Android Studio's own modules are not all the IDE shell

The source tree separates Android plugin functionality from IntelliJ IDE base and shared tooling. Several useful capabilities appear as individual modules in `tools/base`—for example SDK models, APK parsing, lint, manifest parsing, build-system libraries, and device communication. These are better candidates for dependency-level investigation than attempting to package the complete IDE.

**Decision:** prioritize module-level reuse, not whole-product packaging.

## 4. Preliminary licensing findings

- JetBrains describes the IntelliJ Platform as open source under Apache License 2.0.
- The historical `tools/idea` source snapshot exposes an Apache 2.0 `LICENSE.txt` and a `NOTICE.txt` that calls out separately licensed code. That historical notice is a warning, not proof of the license state of every file in the current moving branch.
- Individual Android Studio and Android tools source files may carry Apache 2.0 headers, but a sample file header does not license an entire repository or all transitive dependencies.
- The Android Studio source checkout includes multiple upstream repositories and prebuilt artifacts. Every copied source file, binary, asset, and dependency needs a license/notice record.

**Decision:** do not copy or bundle upstream Android Studio artifacts yet. Before any adoption, record the exact source SHA, path, file header, applicable LICENSE/NOTICE files, dependency licenses, and required attributions. The planned Apache-2.0 license for original StudioDroid code does not override upstream licenses.

## 5. Recommended technical direction

### Android host
Kotlin application with Android-native lifecycle, file access, permission handling, touch UI, keyboard support, process supervision, and memory profile selection.

### IDE services boundary
Define interfaces for:
- workspace and project model;
- document and editor model;
- diagnostics and language intelligence;
- build and task execution;
- source navigation and symbol search;
- device and debugger communication.

An upstream library must sit behind an interface so it can be replaced if ART or Android API compatibility fails.

### First vertical slice
Before attempting a full IDE:
1. Launch a native Android StudioDroid shell.
2. Open a project folder using Android's Storage Access Framework.
3. List project files.
4. Open, edit, save, and reopen one Kotlin file.
5. Detect the device RAM profile.
6. Build and install the APK on the Redmi 14C.

Only after that works should the project add PSI-backed language intelligence or on-device Gradle compilation.

## 6. RAM policy

The requested product rule remains:
- Total RAM **at or below 6 GiB**: default to `LOW_MEMORY`.
- Total RAM **above 6 GiB**: default to `STANDARD`; do not apply StudioDroid's special low-memory reductions.

The low-memory profile should schedule less concurrent indexing/build work and bound caches, not silently remove editor or language features. Android system memory pressure and OEM process management remain relevant in either profile.

## 7. Outstanding work before Phase 0 can be closed

- [ ] Sync the exact `studio-main` manifest and capture the precise `tools/idea` project SHA.
- [ ] Capture the current branch license/notice files for all three main source projects.
- [ ] Export module dependency graphs for selected IntelliJ and Android plugin components.
- [ ] Identify JDK, AWT/Swing, JCEF, JNI, native host binary, filesystem, process, and OS-specific API dependencies per candidate module.
- [ ] Run a small ART/Android ARM64 compatibility prototype for one candidate library.
- [ ] Publish an evidence-backed reuse list with exact file paths and licenses.

## References

1. [Android Studio source build instructions](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main/studio.md)
2. [Android Studio source checkout instructions](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main/source.md)
3. [Android Studio source manifest](https://android.googlesource.com/platform/manifest/+/refs/heads/studio-main/default.xml)
4. [Android plugin source tree](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main)
5. [Shared Android tooling source tree](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main)
6. [IntelliJ Platform overview and capabilities](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html)
7. [IntelliJ UI testing documentation (Swing/AWT)](https://plugins.jetbrains.com/docs/intellij/integration-tests-ui.html)
8. [IntelliJ embedded browser documentation (JCEF)](https://plugins.jetbrains.com/docs/intellij/embedded-browser-jcef.html)
9. [Android Runtime and ART](https://source.android.com/docs/core/runtime)
10. [Android Studio installation and host requirements](https://developer.android.com/studio/install)
11. [IntelliJ Platform open-source and licensing overview](https://www.jetbrains.com/opensource/intellij-platform/)
12. [Historical tools/idea LICENSE.txt](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main/LICENSE.txt)
13. [Historical tools/idea NOTICE.txt](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main/NOTICE.txt)


## 8. Additional UI evidence: Compose in Android Studio is not Android Compose

The current Android Studio plugin tree contains both `adt-ui` and `adt-ui-compose`.

The `adt-ui-compose/README.md` describes `adt-ui-compose` as the Compose-based counterpart to the Swing-based `adt-ui` module. It also documents `StudioComposePanel`, which wraps a composable in a `JComponent` so that it can be embedded into a Studio tool window or dialog. Its test sample uses Compose Desktop and Jewel's standalone theme.

The module's current `BUILD.bazel` depends on IntelliJ Platform Jewel modules, Skiko, and artifacts explicitly named `compose.foundation.desktop` and `compose.runtime.desktop`.

**Implication:** this module is useful as evidence of Google's Compose adoption inside the desktop IDE, but it is not a ready-made Jetpack Compose Android UI library. We must implement the phone UI with Android-native Compose or Views, and may only reuse visual concepts or isolated algorithms after license and dependency review.

Evidence:
- [adt-ui-compose README](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main/adt-ui-compose/README.md)
- [adt-ui-compose BUILD.bazel](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main/adt-ui-compose/BUILD.bazel)

