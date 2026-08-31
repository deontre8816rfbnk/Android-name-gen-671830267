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

    // Used if the user wants to manually change the assigned MD file later
    private val changeDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            saveMdUriAndPermissions(uri)
            Toast.makeText(this, "Markdown file updated!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Setup Immersive Full Screen Mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // Load the saved MD file URI (if any)
        savedMdUri = sharedPrefs.getString("md_uri", null)

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaceNameGeneratorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        onSaveToMarkdown = { markdown ->
                            pendingMarkdownContent = markdown
                            val currentUri = savedMdUri
                            if (currentUri != null) {
                                // File is already assigned, write directly!
                                writeMarkdownToUri(Uri.parse(currentUri), markdown)
                                pendingMarkdownContent = null
                            } else {
                                // No file assigned yet, ask user to pick one
                                openDocumentLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*"))
                            }
                        },
                        onChangeMdFile = {
                            // Trigger file picker to change the assigned MD file
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

    private fun writeMarkdownToUri(uri: Uri, newTableContent: String) {
        try {
            val existingContent = readTextFromUri(uri) ?: ""

            val finalContent = if (existingContent.contains("| Name |") || existingContent.contains("|------|")) {
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
                if (existingContent.isBlank()) {
                    newTableContent
                } else {
                    existingContent.trimEnd() + "\n\n" + newTableContent
                }
            }

            contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(finalContent)
                }
            }

            Toast.makeText(this, "Names saved to Markdown file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
            // If the file was deleted or moved, clear the saved URI
            if (e is java.io.FileNotFoundException || e is SecurityException) {
                sharedPrefs.edit().remove("md_uri").apply()
                savedMdUri = null
            }
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
