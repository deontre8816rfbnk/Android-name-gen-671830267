package com.example

import androidx.lifecycle.ViewModel
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
    val isCopiedFeedback: Boolean = false
)

class NameGeneratorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        GeneratorUiState(
            slotNames = NameGeneratorEngine.generateBatch(NameStyle.NORMAL, 4),
            selectedStyle = NameStyle.NORMAL
        )
    )
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

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
        val text = _uiState.value.savedNames.joinToString(", ")
        _uiState.update {
            it.copy(
                isSavedModalOpen = true,
                savedModalText = text,
                isCopiedFeedback = false
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
        val parsedList = text.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        _uiState.update {
            it.copy(
                savedNames = parsedList,
                isSavedModalOpen = false
            )
        }
    }

    fun markCopiedFeedback() {
        _uiState.update { it.copy(isCopiedFeedback = true) }
        viewModelScope.launch {
            delay(1600)
            _uiState.update { it.copy(isCopiedFeedback = false) }
        }
    }
}
