package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.MyApplicationTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : ComponentActivity() {

    private val viewModel: NameGeneratorViewModel by viewModels()

    private var pendingMarkdownContent: String? = null

    private val sharedPrefs by lazy { getSharedPreferences("NameGenPrefs", Context.MODE_PRIVATE) }
    private var savedMdUri: String? = null

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && pendingMarkdownContent != null) {
            saveMdUriAndPermissions(uri)
            writeMarkdownToUri(uri, pendingMarkdownContent!!)
            pendingMarkdownContent = null
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val changeDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            saveMdUriAndPermissions(uri)
            readAndProcessNames(uri)
            Toast.makeText(this, "Markdown file linked!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        savedMdUri = sharedPrefs.getString("md_uri", null)

        savedMdUri?.let {
            readAndProcessNames(Uri.parse(it))
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaceNameGeneratorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        onSaveToMarkdown = { markdownRows ->
                            pendingMarkdownContent = markdownRows
                            val currentUri = savedMdUri
                            if (currentUri != null) {
                                writeMarkdownToUri(Uri.parse(currentUri), pendingMarkdownContent!!)
                                pendingMarkdownContent = null
                            } else {
                                openDocumentLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
                            }
                        },
                        onChangeMdFile = {
                            changeDocumentLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
                        }
                    )
                }
            }
        }
    }

    private fun saveMdUriAndPermissions(uri: Uri) {
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            savedMdUri = uri.toString()
            sharedPrefs.edit().putString("md_uri", savedMdUri).apply()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Could not persist file permission: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Appends new names ONLY to the existing table's first column (Names).
     * Never creates a second table if one already exists.
     * Pads empty cells so multi-column tables stay valid.
     */
    private fun writeMarkdownToUri(uri: Uri, newRowsContent: String) {
        try {
            val existingContent = readTextFromUri(uri) ?: ""

            // Extract pure names from the incoming content
            val newNames = newRowsContent.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { line ->
                    // Handle both "| name |" format and plain name
                    if (line.startsWith("|")) {
                        line.split("|").map { it.trim() }.getOrNull(1) ?: line.replace("|", "").trim()
                    } else {
                        line
                    }
                }
                .filter { it.isNotEmpty() && !it.equals("Name", ignoreCase = true) && !it.equals("Names", ignoreCase = true) }

            if (newNames.isEmpty()) {
                Toast.makeText(this, "No valid names to save", Toast.LENGTH_SHORT).show()
                return
            }

            val lines = existingContent.lines().toMutableList()

            // Find header + separator of the first markdown table
            var headerIndex = -1
            var separatorIndex = -1
            var columnCount = 1

            for (i in lines.indices) {
                val line = lines[i].trim()
                if (line.startsWith("|") && line.endsWith("|") && line.contains("---")) {
                    // This is the separator line
                    separatorIndex = i
                    if (i > 0) {
                        headerIndex = i - 1
                        val headerCells = lines[headerIndex].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        columnCount = headerCells.size.coerceAtLeast(1)
                    }
                    break
                }
            }

            val finalContent: String

            if (separatorIndex != -1) {
                // Existing table found → append properly padded rows
                val newRows = newNames.map { name ->
                    val cells = MutableList(columnCount) { "" }
                    cells[0] = name
                    "| " + cells.joinToString(" | ") + " |"
                }
                lines.addAll(separatorIndex + 1, newRows)
                finalContent = lines.joinToString("\n")
            } else {
                // No table at all → create a simple one-column table (only if file is empty of tables)
                val tableHeader = "| Names |\n|------|"
                val rows = newNames.joinToString("\n") { "| $it |" }
                finalContent = if (existingContent.isBlank()) {
                    "$tableHeader\n$rows"
                } else {
                    "${existingContent.trimEnd()}\n\n$tableHeader\n$rows"
                }
            }

            contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(finalContent)
                }
            }

            Toast.makeText(this, "Names added to existing table", Toast.LENGTH_SHORT).show()
            readAndProcessNames(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
            if (e is java.io.FileNotFoundException || e is SecurityException) {
                sharedPrefs.edit().remove("md_uri").apply()
                savedMdUri = null
            }
        }
    }

    private fun readAndProcessNames(uri: Uri) {
        try {
            val existingContent = readTextFromUri(uri) ?: return

            val parsedNames = existingContent.lines()
                .filter { it.trim().startsWith("|") && !it.contains("---") }
                .mapNotNull { row ->
                    val parts = row.split("|").map { p -> p.trim() }
                    // Take the first real cell (Names column)
                    parts.getOrNull(1)?.takeIf { it.isNotEmpty() && !it.equals("Name", true) && !it.equals("Names", true) }
                }

            if (parsedNames.isNotEmpty()) {
                viewModel.processLearnedNames(parsedNames)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readTextFromUri(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
