# Workspace and multi-document design

**Status:** Android app foundation implemented in source. The initial single-file editor smoke test passed on Redmi 14C / Android 16. The workspace and file-management flows still require physical-device verification.

## Goals

Move from a one-document editor to a small mobile IDE shell:
- Select a project folder.
- Browse its folders and files.
- Open multiple documents.
- Keep each document's content, language, cursor, and undo state separate.
- Indicate unsaved changes.
- Save to the original document URI or choose Save As for scratch/new files.

## Android storage boundary

Use Android's Storage Access Framework rather than broad shared-storage permissions.

### Workspace folder selection

1. Launch `ActivityResultContracts.OpenDocumentTree`.
2. Persist the provider's read/write URI grant when supported.
3. Capture the root document ID through `DocumentsContract.getTreeDocumentId`.
4. Query child documents using `buildChildDocumentsUriUsingTree`.
5. Create document URIs through `buildDocumentUriUsingTree`.
6. Create files and folders with `DocumentsContract.createDocument`.
7. Rename and delete entries with `DocumentsContract.renameDocument` and `deleteDocument`.

### Directory model

The UI stores:
- Workspace tree URI (also saved in private app preferences when a persistent grant is available).
- Root document ID.
- Current directory stack (document IDs plus display names).
- Current folder entries (ID, display name, MIME type, URI).

Directory listing, document reads, and document writes must execute on `Dispatchers.IO`. Results are sorted with directories first, then names.

### Document identity

Each opened document uses its tree/document URI string as a stable tab path. Reopening an already-open document selects the existing tab instead of creating a duplicate.

Storage providers vary. Some may not support persistent grants, reliable write streams, sorting, or every optional column. Errors must be surfaced to the user without crashing the editor.

The last selected tree URI is stored in private app preferences only after Android confirms a persisted read grant. On launch, the app checks `persistedUriPermissions` before restoring the root listing. If access is no longer valid, it clears the saved URI and asks the user to select the folder again. The current directory stack and open editor tabs are not restored yet.

## Tab model

Nemo's `nemo-tabs` module provides:
- `TabsManager`
- `NemoTab`
- `TabFile`
- `NemoTabs` UI
- A separate `CodeState` per document
- Dirty tracking and safe close behavior

StudioDroid implements its own `WorkspaceTabFile` whose path is the SAF document URI. The tab manager owns active/inactive document selection and independent editing history.

A dirty tab must not be closed silently. The current prototype blocks closing dirty tabs and asks the user to save first. A later UX milestone can add Save / Discard / Cancel confirmation.

## Language selection

The prototype maps filename extensions to Nemo's language-highlighting modes. This is syntax presentation only; it is not compiler-backed semantic analysis or full IDE language intelligence.

Examples:
- `.kt` / `.kts` → Kotlin
- `.java` → Java
- `.py` → Python
- `.js` / `.ts` → JavaScript / TypeScript
- `.xml` → XML
- `.json` → JSON
- `.gradle` → Gradle

Unknown extensions fall back to plain text.

## Project file operations

The explorer supports creating files and folders in the current directory, renaming entries, and deleting entries through the selected tree URI. Names are trimmed and rejected when empty or containing path separators. File MIME types are inferred from the extension, with `text/plain` as the fallback. A file currently open in an editor tab cannot be deleted until its tab is closed; this avoids leaving a live editor attached to a deleted URI. Provider behavior still varies, and non-empty directory deletion may be refused by the provider.

Renaming an open file updates that tab's name and document URI while preserving its editor state. File and folder operations run off the main thread and refresh the visible directory after a successful operation.

## Save behavior

- Existing documents are written using the tab's persisted document URI.
- Scratch tabs have no path and use Android's Create Document flow.
- Save As writes to the selected URI, then updates the tab identity and marks the document saved.
- Save and file reads must happen off the main thread.
- A write failure leaves the tab dirty and reports an error.

## Known limitations

- Current-directory navigation and open editor tabs are not restored after process death; only the project root URI is restored.
- Workspace restoration, unsaved-tab recovery, and recovery after process death are not implemented.
- The explorer is a collapsible panel rather than a resizable desktop tree.
- No Gradle project import or build execution exists yet.
- Syntax highlighting does not equal PSI/LSP diagnostics or semantic completion.
- Large directories/files need performance testing on the reference phone.
- Some document providers may behave differently from local Downloads/Documents providers.

## Device acceptance checklist

- [ ] Select a project folder and keep permission after app restart.
- [ ] Browse into a subfolder and return to the parent.
- [ ] Open at least two files in separate tabs.
- [ ] Edit both files and switch between tabs without losing content.
- [ ] Verify dirty indicators.
- [ ] Save each document back to its original URI.
- [ ] Confirm a dirty tab cannot be silently closed.
- [ ] Confirm clean tabs can be closed.
- [ ] Test with a directory containing many files.
- [ ] Test a Kotlin file with about 1,000 lines.
- [ ] Reopen the same file and confirm it does not create duplicate tabs.
- [ ] Create a file and verify it appears in the current directory.
- [ ] Create a folder and navigate into it.
- [ ] Rename a file that is not open.
- [ ] Rename an open file and verify its tab still saves successfully.
- [ ] Delete a closed file; verify an open file cannot be deleted.
- [ ] Verify provider errors do not crash the app.

