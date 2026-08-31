package com.example

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
import com.example.ui.theme.MyApplicationTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : ComponentActivity() {

    private val viewModel: NameGeneratorViewModel by viewModels()

    // Holds the markdown content waiting to be written
    private var pendingMarkdownContent: String? = null

    // SAF: Open existing document to append / overwrite
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && pendingMarkdownContent != null) {
            writeMarkdownToUri(uri, pendingMarkdownContent!!)
            pendingMarkdownContent = null
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    // SAF: Create a new document
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri: Uri? ->
        if (uri != null && pendingMarkdownContent != null) {
            writeMarkdownToUri(uri, pendingMarkdownContent!!)
            pendingMarkdownContent = null
        } else {
            Toast.makeText(this, "No file created", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaceNameGeneratorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        onSaveToMarkdown = { markdown ->
                            // Launch SAF to let user pick or create the .md file
                            pendingMarkdownContent = markdown
                            // Prefer opening existing so we can append
                            openDocumentLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
                        }
                    )
                }
            }
        }
    }

    private fun writeMarkdownToUri(uri: Uri, newTableContent: String) {
        try {
            // Take persistable permission so we can access it later if needed
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Read existing content (so we can append instead of overwrite)
            val existingContent = readTextFromUri(uri) ?: ""

            val finalContent = if (existingContent.contains("| Name |") || existingContent.contains("|------|")) {
                // Already has a table → append new rows only
                val newRows = newTableContent
                    .lines()
                    .filter { it.startsWith("|") && !it.contains("Name") && !it.contains("------") }
                    .joinToString("\n")

                if (newRows.isBlank()) {
                    existingContent
                } else {
                    existingContent.trimEnd() + "\n" + newRows + "\n"
                }
            } else {
                // No table yet → write full table (or append after existing notes)
                if (existingContent.isBlank()) {
                    newTableContent
                } else {
                    existingContent.trimEnd() + "\n\n" + newTableContent
                }
            }

            // Write back
            contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(finalContent)
                }
            }

            Toast.makeText(this, "Names saved to Markdown file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
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
