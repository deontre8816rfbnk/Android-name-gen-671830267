package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeneratorUiState(
    val slotNames: List<String> = listOf("", "", "", ""),
    val selectedStyle: NameStyle = NameStyle.NORMAL,
    val savedNames: List<String> = emptyList(),
    val savedSlotsFeedback: Set<Int> = emptySet(),
    val isSavedModalOpen: Boolean = false,
    val savedModalText: String = "",
    val isSaveToMdFeedback: Boolean = false,
    val isCombinationsModalOpen: Boolean = false,
    val combinationsText: String = "",
    val customCombinationsCount: Int = 0
)

class NameGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("NameGenPrefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        GeneratorUiState(
            slotNames = NameGeneratorEngine.generateBatch(NameStyle.NORMAL, 4),
            selectedStyle = NameStyle.NORMAL
        )
    )
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    init {
        // Load saved custom combinations from the device into the engine on startup
        val savedCombos = sharedPrefs.getStringSet("custom_combinations", emptySet()) ?: emptySet()
        if (savedCombos.isNotEmpty()) {
            NameGeneratorEngine.addCustomCombinations(savedCombos.toList())
            _uiState.update { 
                it.copy(customCombinationsCount = NameGeneratorEngine.getCustomCombinations().size) 
            }
        }
    }

    fun onSelectStyle(style: NameStyle) {
        _uiState.update { it.copy(selectedStyle = style) }
    }

    fun generateNames() {
        val currentStyle = _uiState.value.selectedStyle
        val newNames = NameGeneratorEngine.generateBatch(currentStyle, 4)
        _uiState.update { it.copy(slotNames = newNames) }
    }

    fun updateSlotName(index: Int, newName: String) {
        if (index !in 0 until 4) return
        _uiState.update { state ->
            val updated = state.slotNames.toMutableList()
            updated[index] = newName
            state.copy(slotNames = updated)
        }
    }

    fun saveSlot(index: Int) {
        val state = _uiState.value
        if (index !in state.slotNames.indices) return
        val nameToSave = state.slotNames[index].trim()
        if (nameToSave.isEmpty()) return

        _uiState.update { current ->
            current.copy(
                savedNames = current.savedNames + nameToSave,
                savedSlotsFeedback = current.savedSlotsFeedback + index
            )
        }

        viewModelScope.launch {
            delay(1100)
            _uiState.update { current ->
                current.copy(savedSlotsFeedback = current.savedSlotsFeedback - index)
            }
        }
    }

    fun openSavedModal() {
        val text = _uiState.value.savedNames.joinToString("\n")
        _uiState.update {
            it.copy(
                isSavedModalOpen = true,
                savedModalText = text,
                isSaveToMdFeedback = false
            )
        }
    }

    fun closeSavedModal() {
        _uiState.update { it.copy(isSavedModalOpen = false) }
    }

    fun updateSavedModalText(text: String) {
        _uiState.update { it.copy(savedModalText = text) }
    }

    fun applySavedModalUpdate() {
        val text = _uiState.value.savedModalText
        val parsedList = text.split("\n", ",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        _uiState.update {
            it.copy(
                savedNames = parsedList,
                isSavedModalOpen = false
            )
        }
    }

    // Reads directly from whatever is in the text field (supports manual typing/pasting)
    fun getSavedNamesAsMarkdownRows(): String {
        val text = _uiState.value.savedModalText
        val names = text.split("\n", ",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (names.isEmpty()) return ""

        val sb = StringBuilder()
        names.forEach { name ->
            sb.appendLine("| $name |")
        }
        return sb.toString()
    }

    // Erases the text field and internal memory the exact moment the save button is clicked
    fun clearSavedNamesAndText() {
        _uiState.update {
            it.copy(
                savedNames = emptyList(),
                savedModalText = "",
                isSaveToMdFeedback = true
            )
        }
        viewModelScope.launch {
            delay(1600)
            _uiState.update { it.copy(isSaveToMdFeedback = false) }
        }
    }

    // Bridge for Contextual Learning
    fun processLearnedNames(names: List<String>) {
        NameGeneratorEngine.ingestLearnedNames(names)
    }

    // === Custom Combinations ===
    fun openCombinationsModal() {
        val current = NameGeneratorEngine.getCustomCombinations().joinToString("\n")
        _uiState.update {
            it.copy(
                isCombinationsModalOpen = true,
                combinationsText = current,
                customCombinationsCount = NameGeneratorEngine.getCustomCombinations().size
            )
        }
    }

    fun closeCombinationsModal() {
        _uiState.update { it.copy(isCombinationsModalOpen = false) }
    }

    fun updateCombinationsText(text: String) {
        _uiState.update { it.copy(combinationsText = text) }
    }

    fun applyCombinations() {
        val combos = _uiState.value.combinationsText
            .split("\n", ",", " ")
            .map { it.trim().lowercase() }
            .filter { it.length in 2..12 }
            .toSet()

        NameGeneratorEngine.clearCustomCombinations()
        NameGeneratorEngine.addCustomCombinations(combos.toList())

        sharedPrefs.edit().putStringSet("custom_combinations", combos).apply()

        _uiState.update {
            it.copy(
                isCombinationsModalOpen = false,
                customCombinationsCount = NameGeneratorEngine.getCustomCombinations().size
            )
        }
    }
}
