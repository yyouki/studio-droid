

## 9. Editor API and runtime coupling

A review of the current IntelliJ Community editor API confirms that desktop coupling exists in the public editor contract, not only inside the desktop window implementation.

- `platform/editor-ui-api/src/com/intellij/openapi/editor/Editor.java` imports Swing types including `JComponent` and `Border`, and AWT types including `Insets`, `Point`, `Rectangle`, and `MouseEvent`. The documented editor visual parts and coordinate/input operations expose these types.
- `platform/platform-impl/src/com/intellij/openapi/editor/impl/EditorImpl.java` imports Swing containers, scroll panes, timers, transfer handlers, AWT graphics/layout/input, drag-and-drop, and input-method APIs. The current GitHub source view reports 5,993 lines for this implementation file.
- `platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java` is not a plain filesystem wrapper: it calls platform application services, file-type registries, VFS managers, write-access assertions, and modification tracking.
- The IntelliJ Platform documentation identifies PSI, indexing, project model, debugger, and full editor as platform capabilities. PSI is therefore a valuable target for further investigation, but it is not demonstrated to be a standalone library independent of the application host.

**Decision:** do not attempt to package `EditorImpl` unchanged or treat the IntelliJ editor API as a platform-neutral document interface. StudioDroid needs its own Android editor presentation and a separately designed document/workspace boundary. Any attempt to reuse PSI must bootstrap or replace the required platform services and pass an ART/Android API compatibility test.

Evidence:
- [Editor API source](https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/openapi/editor/Editor.java)
- [EditorImpl source](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/editor/impl/EditorImpl.java)
- [VirtualFile source](https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java)
- [IntelliJ Platform architecture and PSI](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html)

## 10. Android editor and toolchain candidates

The detailed comparison is in [EDITOR-AND-TOOLCHAIN-PORTABILITY.md](EDITOR-AND-TOOLCHAIN-PORTABILITY.md).

Key findings:
- **Nemo Code Editor** is a current Compose Multiplatform Android editor library under MIT. The reviewed repository snapshot is commit `2120eec0f8fe2f630f643fd87e002ed0ff623ce8`, release v2.0.0 on 2026-10-30. Its Android module specifies minSdk 24 and JVM target 17. It is a candidate for the Android editor UI, not a substitute for IntelliJ PSI.
- **Sora Editor** is a more feature-rich Android View editor with TextMate, Tree-sitter, and LSP modules, but is LGPL-2.1. Its integration obligations need review before an APK can bundle it.
- **AmrDeveloper CodeView** is MIT but its latest visible repository commit is from 2023-12-30; it is not preferred as the main editor base.
- **Manifest Merger** is exported as a separate Maven library by Android Studio, but its dependencies include common, SDK, and build-system libraries. It should be evaluated later as a build-tool dependency, not presumed to be a small Android component.
- **apksig** is a pure-Java library, but its own upstream documentation says it is meant to be used outside Android devices. Treat it as a remote build/CI candidate, not an on-device app dependency without explicit proof.

**Decision:** compare Nemo and Sora in isolated Android samples before adoption. Nemo is the first candidate to test because it has a permissive MIT license and native Compose integration; Sora remains an alternative if LGPL-2.1 obligations are acceptable. No editor dependency has been added to the application yet.

Evidence:
- [Nemo repository and MIT license](https://github.com/Ma7moud3ly/nemo-editor)
- [Nemo v2.0.0 release](https://github.com/Ma7moud3ly/nemo-editor/releases/tag/v2.0.0)
- [Sora repository and LGPL-2.1 license](https://github.com/Rosemoe/sora-editor)
- [Sora language/LSP modules](https://project-sora.github.io/sora-editor-docs/guide/getting-started)
- [apksig project purpose](https://android.googlesource.com/platform/tools/apksig)
