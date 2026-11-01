# StudioDroid Nemo Editor Prototype

This is an **isolated evaluation app**, not the production StudioDroid application and not proof that IntelliJ Platform or Android Studio Desktop has been ported to Android.

## Purpose

Evaluate [Nemo Code Editor](https://github.com/Ma7moud3ly/nemo-editor) v2.0.0 as an Android-native editing surface before adding any editor dependency to the main app.

The prototype includes:
- Kotlin editor with line numbers, indentation guides, auto-indent, and autocomplete.
- Open document through Android's Storage Access Framework.
- Save as a text document through Android's Storage Access Framework.
- Undo and redo.
- Device-reported RAM and the project's two-profile policy.

## Versions

The sample intentionally follows the toolchain used by the reviewed Nemo v2.0.0 source:
- Android Gradle Plugin 9.5.0
- Gradle 9.7.1 (provided by the GitHub Actions workflow)
- Kotlin Compose compiler plugin 2.4.21
- Compose Multiplatform 1.12.2
- Nemo Code Editor 2.0.0
- Java 17
- compileSdk 37, targetSdk 36, minSdk 24

These versions still need a real CI build. A configuration copied from an upstream project is not itself proof that this isolated sample builds.

## Build with GitHub Actions

1. Open the repository's Actions tab.
2. Select **Build Nemo Editor Prototype**.
3. Press **Run workflow** on the main branch.
4. Open the completed workflow run.
5. Download the prototype debug APK artifact.
6. Install the APK on the Redmi 14C and test the cases below.

The workflow only builds a debug APK. It does not sign a Play Store release.

Latest verified run: [Build Nemo Editor Prototype #4](https://github.com/yyouki/studio-droid/actions/runs/41334286016) — succeeded. The uploaded artifact contains app-debug.apk (18,678,515 bytes). SHA-256: 87621989c7e09abe2964f773630403fdc9e62c593156c92707f049c8bd5fbc73. The APK archive was checked successfully; installation and runtime behavior on a physical device are still unverified.

## Device test checklist

- [ ] Install and launch on Redmi 14C / Android 16.
- [ ] Confirm the screen reports LOW_MEMORY.
- [ ] Type, select, copy, paste, and undo Kotlin code.
- [ ] Open a Kotlin file using the Android document picker.
- [ ] Save the edited content as a new text document.
- [ ] Rotate the phone and reopen the app.
- [ ] Test an external keyboard if available.
- [ ] Test a source file with approximately 1,000 lines.
- [ ] Record app responsiveness and peak memory use if possible.

The maintainer reports that the first five device checks passed. See [the Phase 1 device test report](../../docs/PHASE-1-DEVICE-TEST.md).

## Decision gate

Do not move Nemo into the main app until:
- CI build succeeds.
- APK installs and launches on the physical reference device.
- Editing, open, save, and input behavior are usable.
- Transitive dependency licenses are recorded.
- Memory and large-file behavior are acceptable.

If Nemo fails, test Sora Editor as the alternative, with LGPL-2.1 compliance reviewed before distribution.
