# Roadmap

This roadmap is evidence-driven. A phase is complete only when its acceptance criteria are met and recorded.

## Phase 0 — Feasibility and source audit

- [x] Create public repository and initial product specification.
- [x] Record the two-profile RAM policy.
- [ ] Pin candidate Android Studio and IntelliJ source revisions.
- [ ] Map module dependencies and build requirements.
- [ ] Audit source and dependency licenses.
- [ ] Identify desktop-only APIs, native binaries, and ARM64 gaps.
- [ ] Publish a component compatibility matrix.

**Exit criteria:** a reviewed report identifies what can be reused, adapted, replaced, or cannot currently run on Android.

## Phase 1 — Android host proof of concept

- [ ] Create a minimal Android application shell.
- [ ] Verify application lifecycle and storage access.
- [ ] Implement RAM detection and profile selection.
- [ ] Add logging for profile decisions.
- [ ] Build and install a debug APK on the Redmi 14C.

**Exit criteria:** the app launches on the reference phone, selects LOW_MEMORY at 6 GB, and reports its decision accurately.

## Phase 2 — Editor foundation

- [ ] Select an editor component after license and Android-compatibility review.
- [ ] Implement file open, edit, save, tabs, and basic navigation.
- [ ] Add syntax highlighting for one language.
- [ ] Test large files and lifecycle restoration.

**Exit criteria:** a user can open, edit, save, close, and reopen a source file on the phone.

## Phase 3 — Project model and language support

- [ ] Open a small Gradle project.
- [ ] Display project structure.
- [ ] Add diagnostics and basic completion for one supported language.
- [ ] Prove that required runtimes work on Android ARM64.

**Exit criteria:** project structure and language features work for a documented sample project.

## Phase 4 — Build integration

- [ ] Detect or install a compatible JDK and Android SDK components.
- [ ] Run a small Gradle task.
- [ ] Build a sample APK.
- [ ] Capture logs and actionable build errors.
- [ ] Measure the LOW_MEMORY profile on the reference device.

**Exit criteria:** the sample APK builds reproducibly and installs on a physical Android device.

## Phase 5 — Advanced IDE capabilities

- [ ] Debugging research and proof of concept.
- [ ] Git integration.
- [ ] Refactoring and advanced navigation.
- [ ] Plugin compatibility research.
- [ ] Accessibility, keyboard, touch, and large-screen improvements.

**Exit criteria:** each capability has a tested compatibility statement; unsupported desktop plugins are not advertised as supported.

## Release requirements

No release should be described as an Android Studio port or usable IDE until the relevant features run on a physical Android device. Every release must include tested Android versions, supported ABIs, known limitations, and build provenance.
