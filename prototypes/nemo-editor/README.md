# StudioDroid Nemo Editor Prototype

This is an **isolated evaluation app**, not the production StudioDroid application and not proof that IntelliJ Platform or Android Studio Desktop has been ported to Android.

## Purpose

Evaluate [Nemo Code Editor](https://github.com/Ma7moud3ly/nemo-editor) v2.0.0 as an Android-native editing surface before adding any editor dependency to the main app.

The prototype currently includes:
- Kotlin editor with line numbers, indentation guides, auto-indent, and autocomplete.
- Open a single document through Android's Storage Access Framework.
- Select a workspace folder and browse directory entries using DocumentsContract.
- Open multiple workspace files in separate tabs using Nemo Tabs.
- Independent editor state and undo history per tab, with dirty indicators.
- Save changes to the original document URI or use Save As for a scratch tab.
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

Initial version: [Build Nemo Editor Prototype #4](https://github.com/yyouki/studio-droid/actions/runs/41334286016) succeeded, and its APK was installed and manually tested on Redmi 14C / Android 16. The maintainer reported PASS for Kotlin typing, Open, Save As, and LOW_MEMORY display.

Workspace version: [Build run #8](https://github.com/yyouki/studio-droid/actions/runs/41347911409) is the first CI build of the project explorer and multi-tab changes. Do not install or test a new APK until this run has a successful conclusion and artifact.

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

The maintainer reports that the first five device checks passed. See [the Phase 1 device test report](../../docs/PHASE-1-DEVICE-TEST.md). Workspace details are documented in [WORKSPACE-DESIGN.md](../../docs/WORKSPACE-DESIGN.md).

## Decision gate

Do not move Nemo into the main app until:
- CI build succeeds.
- APK installs and launches on the physical reference device.
- Editing, open, save, and input behavior are usable.
- Transitive dependency licenses are recorded.
- Memory and large-file behavior are acceptable.

If Nemo fails, test Sora Editor as the alternative, with LGPL-2.1 compliance reviewed before distribution.
