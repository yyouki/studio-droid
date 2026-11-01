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
- [x] Confirm the prototype CI build and inspect the produced APK.
- [x] Install and test the prototype on Redmi 14C (manual smoke test; see [PHASE-1-DEVICE-TEST.md](PHASE-1-DEVICE-TEST.md)).
- [x] Test Nemo Code Editor 2.0.0 on Android Runtime / ARM64 through manual Redmi 14C / Android 16 smoke test.

**Progress:** Preliminary source audit is in [PHASE-0-AUDIT.md](PHASE-0-AUDIT.md), and editor/toolchain comparisons are in [EDITOR-AND-TOOLCHAIN-PORTABILITY.md](EDITOR-AND-TOOLCHAIN-PORTABILITY.md). The editor API itself exposes Swing/AWT types; the desktop implementation is not a drop-in Android view. Nemo is the first candidate for an isolated Compose editor test; Sora is an alternative with LSP and Tree-sitter modules but LGPL-2.1. Neither library has been integrated into the production app. The isolated Nemo sample's initial APK was built and manually smoke-tested on Redmi 14C / Android 16. Kotlin editing, Open, Save As, and LOW_MEMORY indicator all passed according to the maintainer. The latest source expands the prototype with SAF folder browsing and tabs; its updated CI build and device test are pending. Large-file, lifecycle, memory-pressure, and language-service tests remain pending.

**Exit criteria:** a reviewed report identifies what can be reused, adapted, replaced, or cannot currently run on Android, with exact revisions, source paths, license findings, and evidence. At least one minimal Android/ART test validates the chosen editor component.

## Phase 1 — Android host proof of concept

- [x] Create a minimal Android application shell.
- [x] Verify editor launch and basic document open/save flow on the reference phone.
- [x] Implement RAM detection and profile selection.
- [ ] Add persistent diagnostic logging for profile decisions.
- [x] Build and install a debug APK on the Redmi 14C.
- [ ] Test lifecycle recreation, external keyboard, and large-file behavior.

**Progress:** The maintainer reports successful physical-device checks for Kotlin typing, Open, Save As, and LOW_MEMORY display on Redmi 14C / Android 16. See [PHASE-1-DEVICE-TEST.md](PHASE-1-DEVICE-TEST.md). The current app only reports the memory profile; actual resource-throttling policies are not implemented.

**Exit criteria:** the app reliably launches on the reference phone, handles basic documents, selects LOW_MEMORY at the exact 6,000,000,000-byte threshold, and passes lifecycle and editor regression checks.

## Phase 2 — Workspace and editor foundation

- [x] Prove the Nemo editor candidate in an isolated Android APK on the reference device (manual smoke test).
- [ ] Complete transitive dependency/license review before production adoption.
- [x] Implement folder/workspace selection using Android Storage Access Framework (source added; updated CI/device verification pending).
- [x] Implement project explorer with directory navigation (source added; updated CI/device verification pending).
- [x] Implement multi-file tabs with independent editor state and dirty indicators (Nemo Tabs module; source added, verification pending).
- [x] Implement save/write-back to the selected document URI (source added; provider/device verification pending).
- [ ] Test large files and lifecycle restoration.

**Exit criteria:** after the updated APK passes CI and physical-device testing, a user can select a project folder, browse files, open several source files in tabs, edit/save them, and restore the workspace after process recreation.

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
