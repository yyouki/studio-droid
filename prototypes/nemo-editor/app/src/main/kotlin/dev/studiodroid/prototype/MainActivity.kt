package dev.studiodroid.prototype

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ma7moud3ly.nemo.NemoCodeEditor
import io.ma7moud3ly.nemo.model.EditorSettings
import io.ma7moud3ly.nemo.model.EditorThemes
import io.ma7moud3ly.nemo.model.Language
import io.ma7moud3ly.nemo.model.rememberCodeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

private const val LOW_MEMORY_LIMIT_BYTES = 6_000_000_000L

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

    val codeState = rememberCodeState(
        code = """
            package sample

            fun main() {
                println("Hello from StudioDroid")
            }
        """.trimIndent(),
        language = Language.KOTLIN
    )

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
    var fileName by remember { mutableStateOf("Main.kt") }
    var status by remember { mutableStateOf("Ready") }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            status = "Opening file..."
            scope.launch {
                try {
                    val (text, name) = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IOException("Cannot open the selected document")

                        val content = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        content to queryDisplayName(context, uri)
                    }

                    codeState.updateText(text)
                    codeState.commitChanges()
                    fileName = name
                    status = "File opened"
                } catch (error: Exception) {
                    status = "Open failed: ${error.message ?: "unknown error"}"
                }
            }
        }
    }

    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            status = "Saving file..."
            val content = codeState.code

            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: throw IOException("Cannot write to the selected destination")

                        output.bufferedWriter(Charsets.UTF_8).use { writer ->
                            writer.write(content)
                        }
                    }

                    codeState.commitChanges()
                    fileName = queryDisplayName(context, uri)
                    status = "File saved"
                } catch (error: Exception) {
                    status = "Save failed: ${error.message ?: "unknown error"}"
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        text = "Android IDE editor prototype",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = memoryStatus.profile,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = memoryStatus.ramLabel,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                Button(onClick = {
                    openDocument.launch(arrayOf("text/*", "application/json", "application/xml"))
                }) {
                    Text("Open")
                }

                OutlinedButton(onClick = {
                    createDocument.launch(fileName.ifBlank { "Main.kt" })
                }) {
                    Text("Save as")
                }

                OutlinedButton(
                    onClick = { codeState.undo() },
                    enabled = codeState.canUndo()
                ) {
                    Text("Undo")
                }

                OutlinedButton(
                    onClick = { codeState.redo() },
                    enabled = codeState.canRedo()
                ) {
                    Text("Redo")
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            NemoCodeEditor(
                state = codeState,
                settings = editorSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface)
            )

            HorizontalDivider()

            Text(
                text = "Experimental • ${memoryStatus.profile} • 6 GB adaptive threshold",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
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

    val profile = if (totalBytes <= LOW_MEMORY_LIMIT_BYTES) {
        "LOW_MEMORY"
    } else {
        "STANDARD"
    }

    val totalGb = totalBytes / 1_000_000_000.0
    val label = String.format(Locale.US, "RAM %.1f GB", totalGb)

    return MemoryStatus(profile = profile, ramLabel = label)
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
        if (it.moveToFirst() && nameColumn >= 0) {
            return it.getString(nameColumn)
        }
    }

    return uri.lastPathSegment
        ?.substringAfterLast(':')
        ?.substringAfterLast('/')
        ?: "Document"
}
