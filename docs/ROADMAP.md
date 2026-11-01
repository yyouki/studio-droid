# Roadmap

This roadmap is evidence-driven. A phase is complete only when its acceptance criteria are met and recorded.

## Phase 0 — Feasibility and source audit

- [x] Create public repository and initial product specification.
- [x] Record the two-profile RAM policy.
- [x] Identify Android Studio source checkout areas and their roles.
- [x] Capture preliminary manifest, Android plugin, and shared-tools source snapshots.
- [x] Publish a preliminary compatibility matrix and major platform blockers.
- [x] Inspect IntelliJ editor API / implementation for Swing and AWT coupling.
- [x] Compare Android-native editor candidates and their licenses.
- [ ] Sync the exact manifest and capture the precise current `tools/idea` project SHA.
- [ ] Complete module-level dependency and native-library mapping.
- [ ] Complete current-branch license and notice audit for all candidate source modules.
- [x] Add an isolated Nemo-based Android sample and GitHub Actions APK build workflow.
- [ ] Confirm the prototype CI build and inspect the produced APK.
- [ ] Install and test the prototype on Redmi 14C.
- [ ] Test at least one candidate library on Android Runtime / ARM64.

**Progress:** Preliminary source audit is in [PHASE-0-AUDIT.md](PHASE-0-AUDIT.md), and editor/toolchain comparisons are in [EDITOR-AND-TOOLCHAIN-PORTABILITY.md](EDITOR-AND-TOOLCHAIN-PORTABILITY.md). The editor API itself exposes Swing/AWT types; the desktop implementation is not a drop-in Android view. Nemo is the first candidate for an isolated Compose editor test; Sora is an alternative with LSP and Tree-sitter modules but LGPL-2.1. Neither library has been integrated into the production app or tested on the Redmi 14C. The isolated Nemo sample source exists; its latest CI build is still pending, so no APK is claimed.

**Exit criteria:** a reviewed report identifies what can be reused, adapted, replaced, or cannot currently run on Android, with exact revisions, source paths, license findings, and evidence. At least one minimal Android/ART test validates the chosen editor component.

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
