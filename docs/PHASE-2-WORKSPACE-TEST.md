# Phase 2 — Workspace explorer device validation

**Build:** [GitHub Actions run #10](https://github.com/yyouki/studio-droid/actions/runs/41348070224)  
**APK:** workspace version with project explorer and tabs  
**Reference device:** Redmi 14C / Android 16 / 6 GB RAM  
**Status:** CI build succeeded; physical-device regression test pending.

## Build verification

- Gradle `:app:assembleDebug`: PASS.
- Artifact upload: PASS.
- ZIP integrity: PASS (`unzip -t`).
- APK extracted from the artifact and SHA-256 recorded.
- APK size: 18,711,283 bytes.
- SHA-256: `c20c2e1b5dea241aa9bd310b1e23d8b96c2df69ebc43603edeac37e37a1d8cb5`.

CI success proves that the Android project compiles and produces an APK. It does not prove that document providers behave correctly on the physical device.

## Test setup

Create a disposable test folder with small sample files so that testing does not overwrite important documents. Suggested structure:

```text
StudioDroidTest/
├── Main.kt
├── README.md
└── app/
    ├── build.gradle.kts
    └── src/
        └── Example.kt
```

## Manual test checklist

### Workspace and explorer
- [ ] Install/update to the workspace APK and launch it.
- [ ] Tap Project and select the `StudioDroidTest` folder.
- [ ] Confirm root files and folders appear.
- [ ] Open the `app` folder.
- [ ] Use Up to return to the project root.
- [ ] Hide and show the explorer panel.
- [ ] Close and reopen the app to note whether workspace selection is restored (currently not implemented; expected limitation).

### Tabs and editing
- [ ] Open `Main.kt` and `README.md`; confirm both appear as tabs.
- [ ] Edit `Main.kt`, switch to `README.md`, and return to `Main.kt`; confirm the edit remains.
- [ ] Edit two tabs and confirm each gets an unsaved/dirty indicator.
- [ ] Open `Main.kt` again; confirm it selects the existing tab rather than adding a duplicate.
- [ ] Confirm a dirty tab cannot be closed silently.
- [ ] Save a dirty tab and confirm its dirty indicator disappears.
- [ ] Close a clean tab.

### File save behavior
- [ ] Edit a file inside the selected workspace and press Save.
- [ ] Reopen the file outside the IDE or in another editor and confirm the content persisted.
- [ ] Use Save As on the scratch `Main.kt` tab and save to a new file.
- [ ] Confirm the saved tab now points to the saved document.

### Regression and device behavior
- [ ] Confirm the RAM indicator still reports LOW_MEMORY.
- [ ] Confirm typing, selection, copy/paste, and undo/redo still work.
- [ ] Test a Kotlin source file around 1,000 lines.
- [ ] Test device rotation while an edited tab is open.
- [ ] Record any crashes, blank explorer results, or document-provider permission errors.

## Known limitations

- Workspace URI selection is not persisted in app saved state across process death yet.
- Create/rename/delete file and folder operations are not implemented.
- Dirty tabs are blocked from closing, but a Save / Discard / Cancel dialog is not implemented yet.
- Language modes are selected by filename extension; this is syntax-highlighting selection, not semantic IDE analysis.
- No Gradle project import/build, IntelliJ PSI, or LSP language server is included yet.
