# Architecture

## 1. Objective

StudioDroid is an Android re-platforming effort based on research into Android Studio Desktop and the IntelliJ Platform. The goal is to preserve as much useful IDE behavior and compatible upstream logic as possible while making the application installable and usable on Android phones.

This is not a promise that the desktop distribution can be copied into an APK unchanged.

## 2. Upstream source map

The Android Studio source-build notes identify these major areas:

| Upstream area | Role | Initial action |
| --- | --- | --- |
| `tools/adt/idea` | Android Studio / Android plugin code | Inventory modules, dependencies, platform APIs, and licenses. |
| `tools/idea` | IntelliJ IDE base | Identify reusable non-UI services and desktop-only APIs. |
| `tools/base` | Shared Android tooling libraries | Check Android runtime and ARM64 compatibility for each dependency. |

Reference: [Android Studio source build notes](https://android.googlesource.com/platform/tools/base/+/mirror-goog-studio-main/studio.md).

Android Studio source builds use a substantial Bazel-based build setup. A successful desktop source build is not equivalent to an Android port.

## 3. Platform boundary

The project must separate reusable IDE logic from desktop-specific integration.

### Android host

Responsibilities:
- Application lifecycle, activities, services, permissions, and process management.
- Android Storage Access Framework integration for project files.
- Android-native navigation, windows, dialogs, keyboard handling, and touch interactions.
- Memory-profile detection and runtime policy.
- Safe process supervision for build and language tools.

Candidate implementation: Kotlin and Android Jetpack components. The exact UI framework will be selected after a small device prototype.

### IDE compatibility layer

Responsibilities:
- Isolate upstream APIs behind interfaces.
- Adapt project model, workspace, settings, diagnostics, and editor services.
- Replace unsupported desktop integrations instead of allowing them to leak into Android code.
- Keep upstream-specific code and StudioDroid-owned code clearly separated.

### Editor and language services

Responsibilities:
- Editing, tabs, syntax highlighting, navigation, diagnostics, and completion.
- Language services run only when their runtime and dependencies are compatible with Android or an explicitly supported execution environment.
- Expensive services must support cancellation, incremental work, and bounded resource use.

### Build and toolchain layer

Responsibilities:
- Gradle project import and task execution.
- Android SDK and build-tools discovery.
- Kotlin and Java compilation support.
- Toolchain installation and version management.
- Clear reporting when a required tool is unavailable or incompatible.

The initial implementation must prove that a small Android project can be opened and inspected before promising full project builds.

## 4. Porting strategy

Work component-by-component:

1. Inventory source modules, licenses, native libraries, JDK assumptions, and desktop APIs.
2. Classify each component as reusable, adaptable, replaceable, or unsupported.
3. Build a minimal Android host and prove its lifecycle, storage, and process behavior.
4. Integrate one editor capability at a time.
5. Add project import and build tooling only after ARM64 and Android-runtime compatibility are demonstrated.
6. Add debugging and advanced IDE integrations after the core workflow is stable.

Do not copy the full Android Studio or IntelliJ source tree into this repository as a first step. Keep upstream references pinned to exact revisions and preserve their notices.

## 5. Adaptive memory design

Two default profiles are required:

- **Low-memory:** total physical RAM is at or below 6 GiB.
- **Standard:** total physical RAM is above 6 GiB.

The threshold is a product policy. Detection should use Android system information such as `ActivityManager.MemoryInfo.totalMem`; use `ActivityManager.isLowRamDevice()` and memory-class information as supporting signals and fallbacks. Temporary available-memory pressure may pause work safely, but must not permanently switch a standard device into the low-memory profile.

See [MEMORY-PROFILES.md](MEMORY-PROFILES.md).

## 6. Build and test architecture

The maintainer's reference device is an Android phone, so the repository should support reproducible CI builds. GitHub Actions can run compatible build jobs and publish artifacts, but CI success does not prove that an APK works on a physical phone.

Required validation:
- Static checks and unit tests in CI.
- APK install and launch on a physical Android device.
- Project open/edit/save workflow.
- Memory and responsiveness measurements on a 6 GB-or-lower device.
- Standard-profile regression tests on a device above 6 GB when available.

## 7. Non-goals for the first milestone

- Full compatibility with every Android Studio Desktop plugin.
- Running the desktop Swing UI unchanged inside Android.
- Shipping every Android SDK platform and emulator image inside the APK.
- Claiming support for a compiler, debugger, or language server before it has been tested on Android ARM64.
- Removing editor or language features solely to make the low-memory profile appear faster.

## 8. Open technical questions

- Which IntelliJ Platform services can run on Android Runtime without major modification?
- Which UI and windowing services need Android-native replacements?
- Which upstream modules can legally and technically be redistributed?
- Which Android SDK and Gradle components can be obtained and executed on-device?
- What process model works reliably under Android background-execution limits?
