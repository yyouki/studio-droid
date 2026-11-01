# Phase 1 — Redmi 14C device validation

**Test date:** 2026-11-02  
**Device:** Xiaomi Redmi 14C  
**OS:** Android 16 (reported by maintainer)  
**Memory tier:** marketed 6 GB RAM  
**APK:** Nemo editor prototype debug build from [GitHub Actions run #4](https://github.com/yyouki/studio-droid/actions/runs/41334286016)

## User-reported test results

The maintainer installed and tested the prototype on the reference phone and reported:

| Test | Result | Evidence source |
| --- | --- | --- |
| Open editor and type Kotlin | **PASS** | Maintainer's physical-device test report |
| Open button | **PASS** | Maintainer's physical-device test report |
| Save As button | **PASS** | Maintainer's physical-device test report |
| RAM profile indicator | **PASS — LOW_MEMORY** | Maintainer's physical-device test report |

These are user-reported manual test results, not automated instrumentation or independent device telemetry.

## What this validates

- The APK produced by the CI workflow installs and launches on the maintainer's Android 16 phone.
- The chosen Nemo editor component renders and accepts Kotlin text input on the reference device.
- The Android document picker flow is usable for opening and saving a document in the tested path.
- The prototype reports the LOW_MEMORY profile on the reference device.

## What remains unverified

- Large-file editing and memory use under sustained load.
- Behavior after rotation/process recreation and when returning from background.
- External keyboard shortcuts, IME composition edge cases, copy/paste, and accessibility.
- Folder/workspace navigation and multi-document tabs.
- Whether the low-memory profile actually changes cache sizes, indexing concurrency, or background scheduling. Current prototype only detects/displays the profile.
- IntelliJ PSI or other Android Studio language services on ART.
- Gradle/Android SDK build execution on the phone.

## Phase 1 progress

The initial Android host proof of concept has been demonstrated on the target phone for editor launch, document open/save, and memory-profile reporting. The next milestone is a proper workspace shell: folder selection, project explorer, and multi-file tabs.

## Manual regression checklist

- [x] Install and launch the debug APK on Redmi 14C / Android 16.
- [x] Confirm the editor can open and accept Kotlin input.
- [x] Open a document.
- [x] Save a document using Save As.
- [x] Confirm the LOW_MEMORY indicator.
- [ ] Test a roughly 1,000-line source file.
- [ ] Test rotation and activity recreation.
- [ ] Test external keyboard navigation and common editor shortcuts.
- [ ] Record peak process memory and typing responsiveness.
- [ ] Test repeated open/save and background/foreground cycles.
