# Third-party source and license audit

This file tracks upstream projects that may be studied or integrated. Listing a project here does not mean its code has been copied or that it has been approved for redistribution.

| Project / source area | Upstream location | Intended research | Status |
| --- | --- | --- | --- |
| Android Studio tooling | https://android.googlesource.com/platform/tools/base | Android tooling, Android plugin, shared libraries, build integration | Source and per-file license audit pending |
| IntelliJ Community | https://github.com/JetBrains/intellij-community | IntelliJ Platform and IDE services | Source and dependency audit pending |
| Android platform APIs | https://source.android.com/ | Android runtime, framework, storage, and lifecycle behavior | Review relevant API and component licenses |
| Gradle | https://github.com/gradle/gradle | Build-system integration | Version and license audit pending |
| Kotlin | https://github.com/JetBrains/kotlin | Language tooling and compiler compatibility | Version, runtime, and license audit pending |

## Rules

- Record the exact repository, revision, file paths, and license for every upstream source file copied or modified.
- Keep required copyright headers and license notices.
- Track transitive dependencies and native binaries separately.
- Do not assume that a repository has one license covering every file.
- Do not remove upstream notices or present upstream work as StudioDroid-authored.
- Re-check compatibility and redistribution terms when updating a dependency.

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
