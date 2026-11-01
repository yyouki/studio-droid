# Third-party source and license audit

This file tracks upstream projects that may be studied or integrated. Listing a project here does not mean its code has been copied or that it has been approved for redistribution.

## Preliminary findings (2026-11-02)

| Project / source area | Upstream location | Observed license information | Current decision |
| --- | --- | --- | --- |
| IntelliJ Platform | https://www.jetbrains.com/opensource/intellij-platform/ | JetBrains describes the IntelliJ Platform as Apache License 2.0. | Candidate; verify exact source files and transitive dependencies. |
| IntelliJ Community / `tools/idea` | https://github.com/JetBrains/intellij-community and https://android.googlesource.com/platform/tools/idea | A historical Android Studio `tools/idea` snapshot contains an Apache 2.0 LICENSE and a NOTICE that calls out separately licensed code. The current manifest follows a moving mirror branch, so that historical snapshot is not sufficient for current redistribution approval. | License audit incomplete; do not import a broad source tree yet. |
| Android Studio plugin | https://android.googlesource.com/platform/tools/adt/idea | Public source tree; Android Studio plugin code is built on the IntelliJ Platform. Per-file and dependency license audit not complete. | Candidate for selective adaptation after current-branch audit. |
| Android Studio shared tools | https://android.googlesource.com/platform/tools/base | Individual source files carry license headers; a checked example is Apache 2.0. This does not establish one license for every module and bundled dependency. | Candidate for module-by-module reuse. |
| Android platform APIs | https://source.android.com/ | Android platform components have their own source and component licensing. | Use public API contracts; audit implementation code if copied. |
| Gradle | https://github.com/gradle/gradle | Version and component licensing have not been audited for the selected integration. | Pending. |
| Kotlin | https://github.com/JetBrains/kotlin | Version, compiler, runtime, and dependency licensing have not been audited for the selected integration. | Pending. |

### Source snapshots examined

- Android Studio manifest: [studio-main/default.xml](https://android.googlesource.com/platform/manifest/+/refs/heads/studio-main/default.xml), blob `9df3d97fa144d035edc63338f68b2f4180fed70e`.
- Android plugin: [tools/adt/idea](https://android.googlesource.com/platform/tools/adt/idea/+/refs/heads/mirror-goog-studio-main), commit `224edb8a5a99071fc2fb02bb7a4f80021723ccf6`.
- Shared tools: [tools/base](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main), commit `45628254f0f8a9bb3755d73f3d30ca78fbb2153d`.
- Historical IntelliJ base license: [tools/idea/LICENSE.txt](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main/LICENSE.txt) and [tools/idea/NOTICE.txt](https://android.googlesource.com/platform/tools/idea/+/refs/heads/studio-main/NOTICE.txt). This is a historical branch reference, not a substitute for checking the current manifest-selected revision.

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
