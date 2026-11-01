# Third-party source and license audit

This file tracks upstream projects that may be studied or integrated. Listing a project here does not mean its code has been copied or that it has been approved for redistribution.

## Preliminary findings (2026-11-02)

| Project / source area | Upstream location | Observed license information | Current decision |
| --- | --- | --- | --- |
| IntelliJ Platform | https://www.jetbrains.com/opensource/intellij-platform/ | JetBrains describes the IntelliJ Platform as Apache License 2.0. | Candidate; verify exact source files and transitive dependencies. |
| IntelliJ Community / `tools/idea` | https://github.com/JetBrains/intellij-community and https://android.googlesource.com/platform/tools/idea | A historical Android Studio `tools/idea` snapshot contains an Apache 2.0 LICENSE and a NOTICE that calls out separately licensed code. The current manifest follows a moving mirror branch, so that historical snapshot is not sufficient for current redistribution approval. | License audit incomplete; do not import a broad source tree yet. |
| Android Studio plugin | https://android.googlesource.com/platform/tools/adt/idea | Two sampled files, `android/src/com/android/tools/idea/run/FileSystemApkProvider.java` and `android/src/com/android/tools/idea/run/AndroidRunConfigurationType.java`, carry Apache 2.0 headers (the latter attributes the source to JetBrains). This is file-level evidence only. | Candidate for selective adaptation after current-branch and dependency audit. |
| Android Studio shared tools | https://android.googlesource.com/platform/tools/base | Sampled `common/src/main/java/com/android/SdkConstants.java` carries an Apache 2.0 header. This does not establish one license for every module and bundled dependency. | Candidate for module-by-module reuse. |
| Nemo Code Editor | https://github.com/Ma7moud3ly/nemo-editor | Repository LICENSE is MIT. Reviewed main snapshot commit `2120eec0f8fe2f630f643fd87e002ed0ff623ce8`, release v2.0.0. Transitive dependencies still need audit. | Candidate for Android editor UI prototype; not yet adopted. Retain MIT notice if used. |
| Sora Editor | https://github.com/Rosemoe/sora-editor | Repository LICENSE is GNU LGPL-2.1. Reviewed snapshot commit `94681e9e6523cbdc288e81def76a117f7740a195`. | Candidate alternative; do not bundle until distribution/relinking obligations are reviewed. |
| AmrDeveloper CodeView | https://github.com/AmrDeveloper/CodeView | Repository LICENSE is MIT. Latest visible commit reviewed: `2370ea6bae2e6711755663760f7e23db52991f7b` (2023-12-30). | Lower-priority fallback; no adoption. |
| Android platform APIs | https://source.android.com/ | Android platform components have their own source and component licensing. | Use public API contracts; audit implementation code if copied. |
| Gradle | https://github.com/gradle/gradle | Version and component licensing have not been audited for the selected integration. | Pending. |
| Kotlin | https://github.com/JetBrains/kotlin | Version, compiler, runtime, and dependency licensing have not been audited for the selected integration. | Pending. |
| Android apksig | https://android.googlesource.com/platform/tools/apksig | Android's project documentation describes a pure-Java library and CLI intended for use outside Android devices. The Android Studio BUILD target bundles the apksig library and points to its LICENSE. | Potential remote build/CI dependency; not an on-device app dependency without a separate runtime test. |

### Source snapshots examined

- Android Studio manifest: [studio-main/default.xml](https://android.googlesource.com/platform/manifest/+/refs/heads/studio-main/default.xml), blob `9df3d97fa144d035edc63338f68b2f4180fed70e`.
- Android plugin: [tools/adt/idea](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main), commit `224edb8a5a99071fc2fb02bb7a4f80021723ccf6`.
- Shared tools: [tools/base](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main), commit `45628254f0f8a9bb3755d73f3d30ca78fbb2153d`.
- Historical IntelliJ base license: [tools/idea/LICENSE.txt](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main/LICENSE.txt) and [tools/idea/NOTICE.txt](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main/NOTICE.txt). This is a historical branch reference, not a substitute for checking the current manifest-selected revision.
- Nemo editor: [repository](https://github.com/Ma7moud3ly/nemo-editor), [v2.0.0](https://github.com/Ma7moud3ly/nemo-editor/releases/tag/v2.0.0), [LICENSE](https://github.com/Ma7moud3ly/nemo-editor/blob/main/LICENSE).
- Sora editor: [repository](https://github.com/Rosemoe/sora-editor), [LICENSE](https://github.com/Rosemoe/sora-editor/blob/main/LICENSE).
- CodeView: [repository](https://github.com/AmrDeveloper/CodeView), [LICENSE](https://github.com/AmrDeveloper/CodeView/blob/master/LICENSE).

## Rules

- Record the exact repository, revision, file paths, and license for every upstream source file copied or modified.
- Keep required copyright headers and license notices.
- Track transitive dependencies, fonts, icons, themes, and native binaries separately.
- Do not assume that a repository has one license covering every file.
- Do not remove upstream notices or present upstream work as StudioDroid-authored.
- Do not use Google, Android Studio, JetBrains, or IntelliJ branding in a way that suggests official affiliation.
- Re-check compatibility and redistribution terms when updating a dependency.
- The planned Apache-2.0 license for original StudioDroid code does not override upstream terms.

## Audit record template

For each adopted component, add:
- Component name and purpose.
- Canonical upstream URL.
- Exact tag or commit SHA.
- File paths used or modified.
- License identifier and required notices.
- Android runtime and ARM64 compatibility findings.
- Modifications made by StudioDroid.
- Date and reviewer of the audit.
