# Claude AI handoff — StudioDroid

Use this document as a task packet when asking Claude to review StudioDroid. ChatGPT and Claude do not share live conversation state; the maintainer relays the packet and the review result between them.

## Repository

https://github.com/yyouki/studio-droid

Current task area: isolated Nemo editor prototype under `prototypes/nemo-editor/`.

## Product constraints

- Target is an Android-native IDE inspired by Android Studio Desktop.
- This prototype evaluates an Android editor surface only. It is **not** the IntelliJ Platform port and must not be described as a complete Android Studio port.
- Reference phone: Redmi 14C, Android 16, marketed 6 GB RAM.
- The default profile threshold is exactly 6,000,000,000 bytes (decimal GB): at or below it, LOW_MEMORY; above it, STANDARD.
- LOW_MEMORY must preserve supported user-facing features and optimize resource scheduling/caches; do not remove editor features.
- STANDARD devices must not receive StudioDroid's special low-memory restrictions.
- Preserve upstream license notices and document all dependencies.

## Current prototype

Files:
- `prototypes/nemo-editor/settings.gradle.kts`
- `prototypes/nemo-editor/build.gradle.kts`
- `prototypes/nemo-editor/gradle.properties`
- `prototypes/nemo-editor/app/build.gradle.kts`
- `prototypes/nemo-editor/app/src/main/AndroidManifest.xml`
- `prototypes/nemo-editor/app/src/main/kotlin/dev/studiodroid/prototype/MainActivity.kt`
- `.github/workflows/build-nemo-editor-prototype.yml`

Editor dependency: Nemo Code Editor 2.0.0, MIT, Android Compose Multiplatform.

UI features in source:
- Kotlin editing with line numbers, indent guides, auto-indent and autocomplete.
- Android Storage Access Framework open document and save as.
- Undo and redo.
- Reports device memory and the selected profile.

Device smoke test on Redmi 14C / Android 16 was reported as PASS for Kotlin typing, Open, Save As, and LOW_MEMORY indicator. See `docs/PHASE-1-DEVICE-TEST.md`. Large-file, lifecycle, keyboard, and memory-use testing remain pending.

## Requested Claude review

1. Inspect the latest main-branch version of all files above.
2. Check imports and Nemo 2.0.0 API signatures against the tagged upstream source.
3. Check Compose / AGP / Kotlin / Gradle version compatibility. The prototype copies the Nemo v2.0.0 toolchain: AGP 9.5.0, Gradle 9.7.1, Kotlin Compose plugin 2.4.21, Compose Multiplatform 1.12.2, Java 17, compileSdk 37 minor API 0.
4. Review Android 16 file picker behavior, stream closing, coroutine dispatching, save behavior, and lifecycle handling.
5. Check that the memory threshold is compared as an exact long byte value and that unknown values are not guessed.
6. Review low-memory behavior claims: this prototype currently reports the profile only; it does not yet apply editor/build optimizations.
7. Review the next workspace milestone plan: SAF folder access, project explorer, multi-file tabs, and independent CodeState instances.
8. Identify likely compile/runtime errors and missing tests.
9. Suggest a minimal, focused patch. Do not rewrite unrelated architecture or add new dependencies without explaining why.

## Required response format

- Findings ordered by severity.
- Exact file and line references.
- Proposed patch or precise code changes.
- Build/test commands and what each would prove.
- Anything that could not be verified.

Do not claim a build succeeds unless you actually ran the build. Do not remove license headers or upstream notices. Do not commit changes directly to main; return a review for the maintainer to relay.
