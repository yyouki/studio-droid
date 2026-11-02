# Product development plan

StudioDroid is intended to become one integrated Android-native IDE. The work below describes capabilities of that single product; it is not a sequence of separate apps or releases.

## Application foundation

- [x] Android-native Compose application shell.
- [x] Kotlin editor surface with line numbers, indent guides, auto-indent, and autocomplete.
- [x] Open documents and save to a selected destination using Android Storage Access Framework.
- [x] Select a project folder and browse nested directories.
- [x] Multi-file tabs with independent editor state and dirty indicators.
- [x] Save edited workspace documents back to their original URI.
- [x] Create project files and folders.
- [x] Rename project files and folders.
- [x] Delete project files and folders, with an open-tab safeguard.
- [ ] Restore the selected workspace and editor tabs after process recreation.
- [ ] Recover unsaved editor content safely after interruption.
- [ ] Add robust Save / Discard / Cancel handling for dirty tabs.

The source currently builds through GitHub Actions. The editor-only app was manually smoke-tested on Redmi 14C / Android 16; workspace/file-operation behavior has not yet completed physical-device testing. Source completion and a CI build do not count as device acceptance.

## Project model and language intelligence

- [ ] Detect Android and Kotlin project roots and parse Gradle settings/build files.
- [ ] Build a project tree with Android-specific nodes and project metadata.
- [ ] Add Kotlin language-server integration for diagnostics, completion, symbol lookup, and navigation.
- [ ] Add language-service support for XML and common configuration files.
- [ ] Keep syntax highlighting, parser results, and semantic diagnostics distinct in the UI.
- [ ] Implement incremental indexing and bounded caches.

## Build and runtime

- [ ] Determine a reliable ARM64-compatible JDK and Gradle execution model.
- [ ] Resolve Android SDK components without requiring the user to manually manage a desktop toolchain.
- [ ] Execute Gradle sync and tasks with streaming logs and actionable errors.
- [ ] Build and install a small Android sample APK.
- [ ] Measure build memory, storage use, CPU time, and thermal behavior on the reference phone.
- [ ] Apply the LOW_MEMORY policy to indexing, caches, and build concurrency while preserving supported features.

## IDE interaction and developer tools

- [ ] Improve navigation, search-in-project, replace, and symbol search.
- [ ] Add terminal integration with an Android-compatible shell/runtime model.
- [ ] Add Git status, diff, commit, branch, and conflict workflows.
- [ ] Add debugging feasibility and a working Android-compatible debugger where possible.
- [ ] Improve touch targets, keyboard shortcuts, external keyboard support, accessibility, and large-screen layouts.

## Quality and release

- [ ] Maintain automated build and regression tests.
- [ ] Test lifecycle recreation, URI permission persistence, large projects, large files, and provider compatibility.
- [ ] Verify all bundled dependencies, licenses, and notices.
- [ ] Test on the Redmi 14C and additional Android devices / RAM profiles.
- [ ] Publish versioned debug builds first, then a properly signed release when product quality and security review permit.

## Engineering constraints

- The user-facing product is a single integrated IDE; implementation details should not require the user to install separate component prototypes.
- Android Studio Desktop and IntelliJ editor APIs are not assumed to run unchanged on Android because of Swing/AWT, platform-service, filesystem, runtime, and toolchain dependencies.
- The reference device is Xiaomi Redmi 14C / Android 16 with marketed 6 GB RAM. The profile threshold is exactly 6,000,000,000 bytes; LOW_MEMORY applies at or below the threshold.
- LOW_MEMORY may reduce background concurrency and cache sizes, but must not silently remove supported editor features.
- Builds may run in GitHub Actions because the maintainer's development device is an Android phone.
- A CI artifact is not described as device-tested until it has been installed and exercised on real hardware.
