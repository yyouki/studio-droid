# Editor and toolchain portability decisions

**Review date:** 2026-11-02  
**Status:** Research only. No editor library has been adopted into StudioDroid yet.

## 1. What the IntelliJ editor contract tells us

The IntelliJ Platform is not just a collection of independent parser JARs. JetBrains describes it as a component-driven JVM application host that supplies the editor, project model, build integration, debugging, PSI, and plugin infrastructure.

The current IntelliJ Community source illustrates the coupling directly:

- `platform/editor-ui-api/.../Editor.java` imports `javax.swing.JComponent`, `javax.swing.border.Border`, `java.awt.Insets`, `java.awt.Point`, `java.awt.Rectangle`, `java.awt.event.MouseEvent`, and other desktop types. These are present in the editor API contract, not only its implementation.
- `platform/platform-impl/.../EditorImpl.java` imports many Swing components and AWT classes for layout, rendering, input, scrolling, drag-and-drop, and focus.
- The editor implementation is a large desktop component (the GitHub file view reports 5,993 lines). Replacing only a few imports would not produce a working Android editor.
- IntelliJ's `VirtualFile` abstraction is also tied to platform services such as `ApplicationManager`, file-type registries, VFS managers, read/write actions, and modification tracking. It is not simply a wrapper around `java.io.File`.

Evidence:
- [Editor API source](https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/openapi/editor/Editor.java)
- [EditorImpl source](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/editor/impl/EditorImpl.java)
- [VirtualFile source](https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java)
- [IntelliJ Platform architecture and PSI](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html)

## 2. Important distinction: PSI is valuable, but not a drop-in engine

PSI is one of the main reasons to investigate IntelliJ reuse. It provides parsed syntax/semantic models, indexes, navigation, inspections, completion, and refactoring. However, PSI operates inside the IntelliJ Platform's project, VFS, indexing, threading, application-service, and plugin infrastructure.

Therefore:

- Do not assume that adding an IntelliJ PSI JAR to an Android project is enough.
- Do not claim IntelliJ language plugins will work after only replacing the editor widget.
- Investigate a minimal headless PSI bootstrap separately from the Android editor.
- If the required platform services pull in AWT/Swing or desktop APIs, isolate or replace those services rather than packaging the desktop platform unchanged.

## 3. Android editor candidates (not adopted)

### Candidate A — Nemo Code Editor

Repository: https://github.com/Ma7moud3ly/nemo-editor  
License: MIT  
Reviewed snapshot: `main` commit `2120eec0f8fe2f630f643fd87e002ed0ff623ce8`, release `v2.0.0` (2026-10-30).

The project is a Compose Multiplatform editor component. Its README lists:
- Android, iOS, desktop, JavaScript, and WebAssembly targets.
- Syntax highlighting for Kotlin, Python, JavaScript, TypeScript, C/C++, Go, Rust, Swift, Dart, PHP, HTML, CSS, JSON, Markdown, SQL, and other languages via generic fallback.
- Editing state, selection, cursor control, undo/redo, change tracking, indentation, and autocomplete.
- Optional `nemo-search` and `nemo-tabs` modules.

Its current Android library build configuration sets a minimum SDK of 24 and JVM target 17. The root version catalog currently declares Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.5.0, compileSdk/targetSdk 37, and Gradle wrapper 9.7.1.

Advantages:
- MIT license is compatible with a permissive original-code license strategy, subject to retaining notices and auditing transitive dependencies.
- Android UI is a first-class target.
- Compose Multiplatform fits an Android-native host.
- Search and tab UI are modular.

Risks / unverified:
- The project is very new (2.0.0); no StudioDroid integration or Redmi 14C performance test has been run.
- The current toolchain versions may not match the Android build environment selected for StudioDroid.
- README examples show built-in language intelligence primarily for Kotlin and MicroPython; broad language highlighting is not equivalent to full compiler-backed IDE completion.
- No built-in IntelliJ PSI bridge or Language Server Protocol service was verified in the reviewed README.
- Need inspect the exact released AAR and transitive dependencies, not assume the source build's dependency graph is small.

Preliminary classification: **strong candidate for the Android editor UI prototype; not an IntelliJ engine replacement.**

References:
- [Nemo repository](https://github.com/Ma7moud3ly/nemo-editor)
- [Nemo v2.0.0 release](https://github.com/Ma7moud3ly/nemo-editor/releases/tag/v2.0.0)
- [Nemo editor module build](https://github.com/Ma7moud3ly/nemo-editor/blob/main/nemo-editor/build.gradle.kts)
- [Nemo license](https://github.com/Ma7moud3ly/nemo-editor/blob/main/LICENSE)

### Candidate B — Sora Editor

Repository: https://github.com/Rosemoe/sora-editor  
License: LGPL-2.1  
Reviewed snapshot: `main` commit `94681e9e6523cbdc288e81def76a117f7740a195` (committer date 2026-10-23).

The Android View-based editor offers modules for:
- Core editor.
- Language Server Protocol integration via `editor-lsp`.
- TextMate language grammars.
- Tree-sitter incremental parsing/highlighting.
- Java token-based highlighting/autocomplete.

Its documentation lists JDK 17+ for Gradle builds, Android API 21+ for the editor, and API 26+ when using its LSP module.

Advantages:
- Mobile-oriented editor interaction and rendering.
- Explicit LSP and Tree-sitter modules.
- More direct Android View integration.

Risks:
- LGPL-2.1 obligations must be reviewed for the chosen AAR/source integration and APK distribution model.
- It is a separate Android editor implementation, not the IntelliJ editor engine.
- Device performance and project integration remain untested.

Preliminary classification: **technically strong alternative if we accept and comply with LGPL-2.1; do not bundle before licensing review.**

References:
- [Sora repository](https://github.com/Rosemoe/sora-editor)
- [Sora getting started](https://project-sora.github.io/sora-editor-docs/guide/getting-started)
- [Sora editor overview](https://project-sora.github.io/sora-editor-docs/guide/editor-overview)
- [Sora license](https://github.com/Rosemoe/sora-editor/blob/main/LICENSE)

### Candidate C — AmrDeveloper CodeView

Repository: https://github.com/AmrDeveloper/CodeView  
License: MIT  
Reviewed latest repository commit: `2370ea6bae2e6711755663760f7e23db52991f7b` (2023-12-30).

It offers an Android code-editor view with syntax highlighting, autocomplete, snippets, auto-indent, line numbers, and matching tokens. Its last visible repository commit is old compared with the other candidates.

Preliminary classification: **not preferred as the main editor foundation due to older visible maintenance activity and a narrower feature set.**

References:
- [CodeView repository](https://github.com/AmrDeveloper/CodeView)
- [CodeView documentation](https://amrdeveloper.github.io/CodeView/)

## 4. Android Studio shared tooling candidates

### Manifest Merger

The current `tools/base/build-system/BUILD` exports `com.android.tools.build:manifest-merger` as a Maven library. Its listed dependencies include `tools.common`, `sdk-common`, `sdklib`, Gson, KXML, and Kotlin standard library.

This is a separately published tool library, but the dependency graph is not tiny:
- The current `sdk-common` target depends on analytics shared, annotations, AAPT compiler, builder model, DDMLib, layoutlib API, repository, sdklib, protobuf, Guava, Bouncy Castle, Trove, and other components.
- Some targets in the IntelliJ-oriented module description also refer to IntelliJ SDK dependencies.

Preliminary classification: **candidate for later build-tool integration, but not an early Android app dependency. Map its resolved Maven graph and Android API compatibility first.**

References:
- [Current build-system BUILD file](https://android.googlesource.com/platform/tools/base/+/refs/heads/studio-main/build-system/BUILD)
- [sdk-common BUILD file](https://android.googlesource.com/platform/tools/base/+/studio-master-dev/sdk-common/BUILD)

### APK Signature Library (apksig)

The official project describes apksig as a pure-Java library and command-line tool, and explicitly says it is meant to be used outside Android devices. The Android Studio build publishes it as `com.android.tools.build:apksig`.

Preliminary classification: **useful candidate for a remote build/signing host or CI; do not treat it as an on-device Android app dependency without a separate compatibility proof.**

References:
- [apksig project](https://android.googlesource.com/platform/tools/apksig)
- [Android Studio build target](https://android.googlesource.com/platform/tools/base/+/refs/heads/studio-main/build-system/BUILD)

## 5. Architecture choice

The audit supports this staged architecture:

1. **Android presentation layer:** Android-native Compose UI, with a mobile editor widget selected after a small proof of concept.
2. **Document/workspace model:** StudioDroid-owned abstractions for open documents, project tree, file identity, save state, diagnostics, and editor state.
3. **Language intelligence boundary:** use a service interface. Evaluate (a) a minimal IntelliJ PSI runtime, (b) language servers over LSP, and (c) Tree-sitter for parsing/highlighting. Do not assume these are interchangeable: Tree-sitter provides syntax trees, not the full semantic behavior of an IDE.
4. **Build/toolchain boundary:** test on-device Gradle/JDK/Android SDK support separately; remote CI/build execution is a fallback for host-only tools.
5. **Upstream reuse:** add Android Studio/IntelliJ components only after dependency, license, and ART/ARM64 verification.

### Candidate selection gate

Before adopting an editor library, create an isolated Android sample and verify:
- AAR resolves from Maven Central or a pinned source revision.
- Builds with the StudioDroid-selected Kotlin/Compose/AGP/Gradle versions.
- APK installs and runs on Redmi 14C / Android 16.
- Handles a representative large Kotlin file without unacceptable memory spikes or typing latency.
- Supports external keyboard navigation, touch selection, IME composition, copy/paste, and undo/redo.
- Can exchange diagnostics and completion results with a separate language-service interface.
- All upstream and transitive licenses are recorded.

## 6. Current decision

**Do not attempt a direct Android port of IntelliJ EditorImpl.**

The next proof of concept should compare Nemo and Sora in isolated Android sample apps. Nemo is the first candidate to evaluate because of its MIT license and native Compose integration; Sora is the feature-rich alternative if its LGPL-2.1 obligations are acceptable. This is a provisional test order, not a final adoption decision.

No editor dependency should be added to the main StudioDroid app until the sample has passed the device and licensing gates above.
