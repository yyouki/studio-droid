package dev.studiodroid.prototype

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ma7moud3ly.nemo.NemoCodeEditor
import io.ma7moud3ly.nemo.model.EditorSettings
import io.ma7moud3ly.nemo.model.EditorThemes
import io.ma7moud3ly.nemo.model.Language
import io.ma7moud3ly.nemo.tabs.NemoTabs
import io.ma7moud3ly.nemo.tabs.TabFile
import io.ma7moud3ly.nemo.tabs.TabsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

private const val LOW_MEMORY_LIMIT_BYTES = 6_000_000_000L
private const val WORKSPACE_PREFERENCES = "studio_droid_workspace"
private const val KEY_WORKSPACE_TREE_URI = "workspace_tree_uri"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StudioDroidEditorScreen()
            }
        }
    }
}

@Composable
private fun StudioDroidEditorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tabsManager = remember { TabsManager() }
    val activeTab by tabsManager.activeTabFlow.collectAsState(initial = null)
    val openTabs = tabsManager.tabs

    val editorSettings = remember {
        EditorSettings(
            theme = EditorThemes.VS_CODE_DARK,
            tabSize = 4,
            showLineNumbers = true,
            showIndentGuides = true,
            enableAutoIndent = true,
            enableAutocomplete = true
        )
    }

    val memoryStatus = remember(context) { readMemoryStatus(context) }
    var status by remember { mutableStateOf("Ready") }
    var workspaceUri by remember { mutableStateOf<Uri?>(null) }
    var workspaceName by remember { mutableStateOf("No project selected") }
    var rootDocumentId by remember { mutableStateOf<String?>(null) }
    var folderStack by remember { mutableStateOf(emptyList<WorkspaceFolder>()) }
    var folderEntries by remember { mutableStateOf(emptyList<WorkspaceEntry>()) }
    var showExplorer by remember { mutableStateOf(true) }
    var savingTabId by remember { mutableStateOf<String?>(null) }
    var explorerDialog by remember { mutableStateOf<ExplorerDialogState?>(null) }
    var dialogName by remember { mutableStateOf("") }

    // Add a scratch tab so a new installation opens directly into an editor.
    LaunchedEffect(tabsManager) {
        if (tabsManager.tabs.isEmpty()) {
            tabsManager.addTab(
                document = WorkspaceTabFile("Main.kt", ""),
                content = """
                    package sample

                    fun main() {
                        println("Hello from StudioDroid")
                    }
                """.trimIndent(),
                language = Language.KOTLIN
            )
        }
    }

    // Restore the last project only when Android still grants persistent tree access.
    LaunchedEffect(Unit) {
        val preferences = context.getSharedPreferences(
            WORKSPACE_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val savedUri = preferences.getString(KEY_WORKSPACE_TREE_URI, null)
        if (!savedUri.isNullOrBlank()) {
            val restoredUri = Uri.parse(savedUri)
            val hasReadGrant = context.contentResolver.persistedUriPermissions.any {
                it.uri == restoredUri && it.isReadPermission
            }

            if (!hasReadGrant) {
                preferences.edit().remove(KEY_WORKSPACE_TREE_URI).apply()
                status = "Project access expired. Select the project folder again."
            } else {
                try {
                    val restoredRootId = DocumentsContract.getTreeDocumentId(restoredUri)
                    workspaceUri = restoredUri
                    rootDocumentId = restoredRootId
                    workspaceName = queryDisplayName(
                        context,
                        DocumentsContract.buildDocumentUriUsingTree(
                            restoredUri,
                            restoredRootId
                        )
                    )
                    refreshFolder(restoredUri, restoredRootId)
                    showExplorer = true
                    status = "Restored project " + workspaceName
                } catch (error: Exception) {
                    status = "Project restore failed: " +
                        (error.message ?: "select the project folder again")
                }
            }
        }
    }

    suspend fun refreshFolder(treeUri: Uri, documentId: String) {
        folderEntries = withContext(Dispatchers.IO) {
            listWorkspaceChildren(context, treeUri, documentId)
        }
    }

    fun openFile(uri: Uri, name: String) {
        // Reuse an existing tab when the same document is opened again.
        val existing = tabsManager.findTab(uri.toString())
        if (existing != null) {
            tabsManager.switchTab(existing.id)
            status = "Switched to " + name
            return
        }

        status = "Opening " + name + "..."
        scope.launch {
            try {
                val fileText = withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: throw IOException("Cannot open the selected document")
                    input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                }
                tabsManager.openTab(
                    document = WorkspaceTabFile(name, uri.toString()),
                    content = fileText,
                    language = languageForFile(name)
                )
                status = "Opened " + name
            } catch (error: Exception) {
                status = "Open failed: " + (error.message ?: "unknown error")
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Keep using the provider's temporary grant when persistence is unavailable.
            }
            openFile(uri, queryDisplayName(context, uri))
        }
    }

    val openWorkspace = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentAccess = try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                context.contentResolver.persistedUriPermissions.any {
                    it.uri == uri && it.isReadPermission
                }
            } catch (_: SecurityException) {
                // Some providers grant access only for this session.
                false
            }

            val preferences = context.getSharedPreferences(
                WORKSPACE_PREFERENCES,
                Context.MODE_PRIVATE
            )
            if (persistentAccess) {
                preferences.edit().putString(KEY_WORKSPACE_TREE_URI, uri.toString()).apply()
            } else {
                // Avoid restoring a URI that will no longer be accessible after process death.
                preferences.edit().remove(KEY_WORKSPACE_TREE_URI).apply()
            }

            val rootId = DocumentsContract.getTreeDocumentId(uri)
            workspaceUri = uri
            rootDocumentId = rootId
            folderStack = emptyList()
            workspaceName = queryDisplayName(
                context,
                DocumentsContract.buildDocumentUriUsingTree(uri, rootId)
            )
            status = "Loading project..."
            scope.launch {
                try {
                    refreshFolder(uri, rootId)
                    showExplorer = true
                    status = if (persistentAccess) {
                        "Project opened"
                    } else {
                        "Project opened for this session only"
                    }
                } catch (error: Exception) {
                    folderEntries = emptyList()
                    status = "Project error: " + (error.message ?: "unknown error")
                }
            }
        }
    }

    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        val tabId = savingTabId
        savingTabId = null

        if (uri != null && tabId != null) {
            val tab = tabsManager.getTab(tabId)
            if (tab != null) {
                val textToSave = tab.codeState.code
                status = "Saving " + tab.title + "..."
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val output = context.contentResolver.openOutputStream(uri, "wt")
                                ?: throw IOException("Cannot write to the selected destination")
                            output.bufferedWriter(Charsets.UTF_8).use { it.write(textToSave) }
                        }
                        val savedName = queryDisplayName(context, uri)
                        tabsManager.updateTab(
                            tabId,
                            WorkspaceTabFile(savedName, uri.toString())
                        )
                        tabsManager.commitChanges(tabId)
                        status = "Saved " + savedName
                    } catch (error: Exception) {
                        status = "Save failed: " + (error.message ?: "unknown error")
                    }
                }
            }
        }
    }

    fun saveTab(tabId: String, saveAs: Boolean = false) {
        val tab = tabsManager.getTab(tabId) ?: return
        if (saveAs || tab.path.isBlank()) {
            savingTabId = tabId
            createDocument.launch(tab.title.ifBlank { "Untitled.txt" })
            return
        }

        val textToSave = tab.codeState.code
        val destination = Uri.parse(tab.path)
        status = "Saving " + tab.title + "..."
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(destination, "wt")
                        ?: throw IOException("Cannot write to the selected file")
                    output.bufferedWriter(Charsets.UTF_8).use { it.write(textToSave) }
                }
                tabsManager.commitChanges(tabId)
                status = "Saved " + tab.title
            } catch (error: Exception) {
                status = "Save failed: " + (error.message ?: "unknown error")
            }
        }
    }

    suspend fun applyExplorerAction(dialog: ExplorerDialogState, inputName: String) {
        val tree = workspaceUri ?: throw IOException("Select a project folder first")
        val parentId = folderStack.lastOrNull()?.documentId ?: rootDocumentId
            ?: throw IOException("Project folder is unavailable")
        val resolver = context.contentResolver
        val entry = dialog.entry

        when (dialog.action) {
            ExplorerAction.CREATE_FILE -> {
                val name = validateEntryName(inputName)
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(tree, parentId)
                val createdUri = withContext(Dispatchers.IO) {
                    DocumentsContract.createDocument(
                        resolver,
                        parentUri,
                        mimeTypeForFile(name),
                        name
                    ) ?: throw IOException("The storage provider did not create the file")
                }
                refreshFolder(tree, parentId)
                openFile(createdUri, name)
                status = "Created " + name
            }

            ExplorerAction.CREATE_FOLDER -> {
                val name = validateEntryName(inputName)
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(tree, parentId)
                withContext(Dispatchers.IO) {
                    DocumentsContract.createDocument(
                        resolver,
                        parentUri,
                        DocumentsContract.Document.MIME_TYPE_DIR,
                        name
                    ) ?: throw IOException("The storage provider did not create the folder")
                }
                refreshFolder(tree, parentId)
                status = "Created folder " + name
            }

            ExplorerAction.RENAME -> {
                val target = entry ?: throw IOException("No file selected")
                val name = validateEntryName(inputName)
                val renamedUri = withContext(Dispatchers.IO) {
                    DocumentsContract.renameDocument(resolver, target.uri, name)
                        ?: throw IOException("The storage provider could not rename this item")
                }
                tabsManager.findTab(target.uri.toString())?.let { tab ->
                    tabsManager.updateTab(
                        tab.id,
                        WorkspaceTabFile(name, renamedUri.toString())
                    )
                }
                refreshFolder(tree, parentId)
                status = "Renamed to " + name
            }

            ExplorerAction.DELETE -> {
                val target = entry ?: throw IOException("No file selected")
                // Do not leave an editor tab pointing at a document that has been deleted.
                if (tabsManager.findTab(target.uri.toString()) != null) {
                    throw IOException("Close this item's editor tab before deleting it")
                }
                val deleted = withContext(Dispatchers.IO) {
                    DocumentsContract.deleteDocument(resolver, target.uri)
                }
                if (!deleted) throw IOException("The storage provider refused to delete this item")
                refreshFolder(tree, parentId)
                status = "Deleted " + target.name
            }
        }
    }

    val currentFolder = folderStack.lastOrNull()
    val currentFolderId = currentFolder?.documentId ?: rootDocumentId
    val currentFolderName = currentFolder?.name ?: workspaceName

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "StudioDroid",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android IDE workspace prototype",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(memoryStatus.profile, style = MaterialTheme.typography.labelLarge)
                    Text(memoryStatus.ramLabel, style = MaterialTheme.typography.bodySmall)
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { openWorkspace.launch(null) }) {
                    Text("Project")
                }
                OutlinedButton(onClick = {
                    openDocument.launch(arrayOf("text/*", "application/json", "application/xml"))
                }) {
                    Text("Open file")
                }
                OutlinedButton(
                    onClick = { activeTab?.let { saveTab(it.id) } },
                    enabled = activeTab != null
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { activeTab?.let { saveTab(it.id, saveAs = true) } },
                    enabled = activeTab != null
                ) {
                    Text("Save as")
                }
                OutlinedButton(
                    onClick = { activeTab?.codeState?.undo() },
                    enabled = activeTab?.codeState?.canUndo() == true
                ) {
                    Text("Undo")
                }
                OutlinedButton(
                    onClick = { activeTab?.codeState?.redo() },
                    enabled = activeTab?.codeState?.canRedo() == true
                ) {
                    Text("Redo")
                }
                OutlinedButton(
                    onClick = { showExplorer = !showExplorer },
                    enabled = workspaceUri != null
                ) {
                    Text(if (showExplorer) "Hide files" else "Show files")
                }
            }

            if (workspaceUri != null && showExplorer) {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PROJECT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentFolderName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val tree = workspaceUri
                            if (tree != null && folderStack.isNotEmpty()) {
                                folderStack = folderStack.dropLast(1)
                                val targetId = folderStack.lastOrNull()?.documentId ?: rootDocumentId
                                if (targetId != null) {
                                    status = "Loading folder..."
                                    scope.launch {
                                        try {
                                            refreshFolder(tree, targetId)
                                            status = "Folder loaded"
                                        } catch (error: Exception) {
                                            status = "Folder error: " + (error.message ?: "unknown error")
                                        }
                                    }
                                }
                            }
                        },
                        enabled = folderStack.isNotEmpty()
                    ) {
                        Text("Up")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            dialogName = "NewFile.kt"
                            explorerDialog = ExplorerDialogState(ExplorerAction.CREATE_FILE)
                        }
                    ) { Text("New file") }
                    TextButton(
                        onClick = {
                            dialogName = "NewFolder"
                            explorerDialog = ExplorerDialogState(ExplorerAction.CREATE_FOLDER)
                        }
                    ) { Text("New folder") }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 168.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    items(folderEntries, key = { it.documentId }) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val tree = workspaceUri ?: return@TextButton
                                    if (entry.isDirectory) {
                                        folderStack = folderStack + WorkspaceFolder(
                                            entry.documentId,
                                            entry.name
                                        )
                                        status = "Loading " + entry.name + "..."
                                        scope.launch {
                                            try {
                                                refreshFolder(tree, entry.documentId)
                                                status = "Folder opened"
                                            } catch (error: Exception) {
                                                status = "Folder error: " + (error.message ?: "unknown error")
                                            }
                                        }
                                    } else {
                                        openFile(entry.uri, entry.name)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(if (entry.isDirectory) "▸  " else "   ")
                                    Text(
                                        text = entry.name,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            TextButton(onClick = {
                                dialogName = entry.name
                                explorerDialog = ExplorerDialogState(
                                    ExplorerAction.RENAME,
                                    entry
                                )
                            }) { Text("Rename") }
                            TextButton(onClick = {
                                explorerDialog = ExplorerDialogState(
                                    ExplorerAction.DELETE,
                                    entry
                                )
                            }) { Text("Delete") }
                        }
                    }
                }
                HorizontalDivider()
            }

            Text(
                text = status,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall
            )

            NemoTabs(
                tabs = openTabs,
                activeTabId = activeTab?.id,
                onSelect = { tabsManager.switchTab(it.id) },
                onClose = { tab ->
                    if (!tabsManager.closeTab(tab.id)) {
                        status = "Save changes before closing " + tab.title
                    }
                }
            )

            val currentTab = activeTab
            if (currentTab != null) {
                NemoCodeEditor(
                    state = currentTab.codeState,
                    settings = editorSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No open editor", style = MaterialTheme.typography.titleMedium)
                    Text("Open a file from the project explorer or create a new tab.")
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTab?.title ?: "No file",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
                Text(
                    text = memoryStatus.profile + " • 6 GB threshold",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    val activeDialog = explorerDialog
    if (activeDialog != null) {
        val title = when (activeDialog.action) {
            ExplorerAction.CREATE_FILE -> "Create file"
            ExplorerAction.CREATE_FOLDER -> "Create folder"
            ExplorerAction.RENAME -> "Rename item"
            ExplorerAction.DELETE -> "Delete item?"
        }
        AlertDialog(
            onDismissRequest = { explorerDialog = null },
            title = { Text(title) },
            text = {
                if (activeDialog.action == ExplorerAction.DELETE) {
                    Text(
                        "Delete \"" + (activeDialog.entry?.name ?: "item") +
                            "\"? This cannot be undone."
                    )
                } else {
                    TextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        singleLine = true,
                        label = { Text("Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val action = activeDialog
                    val name = dialogName
                    if (action.action != ExplorerAction.DELETE && name.isBlank()) {
                        status = "Name cannot be empty"
                    } else {
                        scope.launch {
                            try {
                                applyExplorerAction(action, name)
                                explorerDialog = null
                            } catch (error: Exception) {
                                status = "File operation failed: " +
                                    (error.message ?: "unknown error")
                            }
                        }
                    }
                }) {
                    Text(if (activeDialog.action == ExplorerAction.DELETE) "Delete" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { explorerDialog = null }) { Text("Cancel") }
            }
        )
    }
}

private enum class ExplorerAction {
    CREATE_FILE,
    CREATE_FOLDER,
    RENAME,
    DELETE
}

private data class ExplorerDialogState(
    val action: ExplorerAction,
    val entry: WorkspaceEntry? = null
)

private data class WorkspaceTabFile(
    override val name: String,
    override val path: String
) : TabFile

private data class WorkspaceFolder(
    val documentId: String,
    val name: String
)

private data class WorkspaceEntry(
    val documentId: String,
    val name: String,
    val mimeType: String,
    val uri: Uri
) {
    val isDirectory: Boolean
        get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
}

private suspend fun listWorkspaceChildren(
    context: Context,
    treeUri: Uri,
    parentDocumentId: String
): List<WorkspaceEntry> {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
        treeUri,
        parentDocumentId
    )
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE
    )
    val entries = mutableListOf<WorkspaceEntry>()

    context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID
        )
        val nameColumn = cursor.getColumnIndexOrThrow(
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )
        val mimeColumn = cursor.getColumnIndexOrThrow(
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        while (cursor.moveToNext()) {
            val documentId = cursor.getString(idColumn) ?: continue
            val name = cursor.getString(nameColumn) ?: "Untitled"
            val mimeType = cursor.getString(mimeColumn) ?: "application/octet-stream"
            entries += WorkspaceEntry(
                documentId = documentId,
                name = name,
                mimeType = mimeType,
                uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            )
        }
    }

    // Sort directories before files, then sort each group by display name.
    return entries.sortedWith(
        compareBy<WorkspaceEntry>({ !it.isDirectory }, { it.name.lowercase(Locale.ROOT) })
    )
}

private fun validateEntryName(rawName: String): String {
    val name = rawName.trim()
    require(name.isNotEmpty()) { "Name cannot be empty" }
    require('/' !in name && '\\' !in name && '\u0000' !in name) {
        "Name cannot contain path separators"
    }
    require(name != "." && name != "..") { "Invalid file or folder name" }
    return name
}

private fun mimeTypeForFile(name: String): String {
    val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
    return android.webkit.MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension)
        ?: "text/plain"
}

private data class MemoryStatus(
    val profile: String,
    val ramLabel: String
)

private fun readMemoryStatus(context: Context): MemoryStatus {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    manager.getMemoryInfo(memoryInfo)

    val totalBytes = memoryInfo.totalMem
    if (totalBytes <= 0L) {
        return MemoryStatus(profile = "UNKNOWN", ramLabel = "RAM unavailable")
    }

    val profile = if (totalBytes <= LOW_MEMORY_LIMIT_BYTES) "LOW_MEMORY" else "STANDARD"
    val totalGb = totalBytes / 1_000_000_000.0
    val label = String.format(Locale.US, "RAM %.1f GB", totalGb)
    return MemoryStatus(profile = profile, ramLabel = label)
}

private fun languageForFile(name: String): Language {
    return when (name.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
        "kt", "kts" -> Language.KOTLIN
        "java" -> Language.JAVA
        "py" -> Language.PYTHON
        "js", "mjs", "cjs" -> Language.JAVASCRIPT
        "ts" -> Language.TYPESCRIPT
        "tsx" -> Language.REACT_TSX
        "jsx" -> Language.REACT_JSX
        "html", "htm" -> Language.HTML
        "css", "scss" -> Language.CSS
        "json" -> Language.JSON
        "xml" -> Language.XML
        "md", "markdown" -> Language.MARKDOWN
        "c" -> Language.C
        "h" -> Language.C_HEADER
        "cc", "cpp", "cxx", "hpp" -> Language.CPP
        "rs" -> Language.RUST
        "go" -> Language.GO
        "swift" -> Language.SWIFT
        "dart" -> Language.DART
        "php" -> Language.PHP
        "rb" -> Language.RUBY
        "sh", "bash" -> Language.SHELL
        "sql" -> Language.SQL
        "yaml", "yml" -> Language.YAML
        "toml" -> Language.TOML
        "gradle" -> Language.GRADLE
        "txt" -> Language.TEXT
        else -> Language.TEXT
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )

    cursor?.use {
        val nameColumn = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameColumn >= 0) return it.getString(nameColumn)
    }

    return uri.lastPathSegment
        ?.substringAfterLast(':')
        ?.substringAfterLast('/')
        ?: "Document"
}
