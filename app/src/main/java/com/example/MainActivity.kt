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

        savedMdUri = sharedPrefs.getString("md_uri", null)

        // If a file is already assigned, read it immediately to learn existing names
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

    private fun writeMarkdownToUri(uri: Uri, newRowsContent: String) {
        try {
            val existingContent = readTextFromUri(uri) ?: ""
            
            // Extract just the names from the incoming markdown rows
            val newNames = newRowsContent.lines()
                .filter { it.trim().startsWith("|") && !it.contains("Name") && !it.contains("---") && !it.contains("------") }
                .map { it.replace("|", "").trim() }
                .filter { it.isNotEmpty() }

            if (newNames.isEmpty()) {
                Toast.makeText(this, "No valid names to save", Toast.LENGTH_SHORT).show()
                return
            }

            // Convert back to strict rows to insert
            val newRowsToInsert = newNames.map { "| $it |" }

            val lines = existingContent.lines().toMutableList()
            var separatorIndex = -1
            
            // Bulletproof table separator detection: finds any line like |---| or | --- | or |------|
            for (i in lines.indices) {
                val line = lines[i].trim()
                if (line.startsWith("|") && line.endsWith("|")) {
                    val inner = line.drop(1).dropLast(1).replace(" ", "")
                    if (inner.isNotEmpty() && inner.all { it == '-' }) {
                        separatorIndex = i
                        break
                    }
                }
            }
            
            val finalContent: String
            
            if (separatorIndex != -1) {
                // Table exists! Insert rows strictly AFTER the separator line.
                lines.addAll(separatorIndex + 1, newRowsToInsert)
                finalContent = lines.joinToString("\n")
            } else {
                // Only if absolutely NO table exists in the file, create one at the bottom
                val tableHeader = "| Name |\n|------|"
                finalContent = if (existingContent.isBlank()) {
                    "$tableHeader\n${newRowsToInsert.joinToString("\n")}"
                } else {
                    "${existingContent.trimEnd()}\n\n$tableHeader\n${newRowsToInsert.joinToString("\n")}"
                }
            }

            contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(finalContent)
                }
            }

            Toast.makeText(this, "Names saved to Markdown file", Toast.LENGTH_SHORT).show()
            
            // After saving, read the file again to update the learning context
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
                .filter { it.trim().startsWith("|") && !it.contains("Name") && !it.contains("---") && !it.contains("------") }
                .mapNotNull { row ->
                    val parts = row.split("|").map { p -> p.trim() }
                    if (parts.size > 1 && parts[1].isNotEmpty()) parts[1] else null
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
