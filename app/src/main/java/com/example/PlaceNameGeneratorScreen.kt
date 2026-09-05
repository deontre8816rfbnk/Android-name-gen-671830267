package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Dark theme colors (matching Tviy)
private val BgDark = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF141414)
private val TextPrimary = Color.White
private val TextMuted = Color.White.copy(alpha = 0.55f)
private val BtnPrimaryBg = Color.White
private val BtnPrimaryText = Color.Black
private val BtnSecondaryBg = Color(0xFF1C1C1C)
private val BtnSecondaryText = Color.White
private val PillBg = Color.White.copy(alpha = 0.08f)
private val PillSelectedBg = Color.White
private val NameBoxBg = Color(0xFF1A1A1A)
private val BorderSubtle = Color.White.copy(alpha = 0.10f)
private val AccentGreen = Color(0xFF10B981)

@Composable
fun PlaceNameGeneratorScreen(
    viewModel: NameGeneratorViewModel,
    modifier: Modifier = Modifier,
    onSaveToMarkdown: (String) -> Unit = {},
    onChangeMdFile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Saved: ${uiState.savedNames.size}",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("saved_count")
                )
            }

            // Title
            Text(
                text = "Place Name Generator",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                textAlign = TextAlign.Center
            )

            // Name boxes
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.slotNames.forEachIndexed { index, name ->
                    NameBoxItem(
                        name = name,
                        isSavedSuccess = index in uiState.savedSlotsFeedback,
                        onNameChange = { viewModel.updateSlotName(index, it) },
                        onSave = { viewModel.saveSlot(index) },
                        index = index
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Style pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NameStyle.entries.forEach { style ->
                        OptionPill(
                            text = style.displayName,
                            isSelected = uiState.selectedStyle == style,
                            onClick = { viewModel.onSelectStyle(style) },
                            modifier = Modifier.testTag("option_btn_${style.key}")
                        )
                    }
                }

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + combinations
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnSecondaryBg)
                            .clickable { viewModel.openCombinationsModal() }
                            .testTag("add_combinations_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add letter combinations",
                            tint = BtnSecondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Generate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnPrimaryBg)
                            .clickable { viewModel.generateNames() }
                            .testTag("generate_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Generate",
                            color = BtnPrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // View Saved
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnSecondaryBg)
                            .clickable { viewModel.openSavedModal() }
                            .testTag("view_saved_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Saved",
                            color = BtnSecondaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Saved Names Modal
        if (uiState.isSavedModalOpen) {
            SavedNamesModal(
                text = uiState.savedModalText,
                isSaveToMd = uiState.isSaveToMdFeedback,
                onTextChange = { viewModel.updateSavedModalText(it) },
                onSaveToMd = {
                    val markdownRows = viewModel.getSavedNamesAsMarkdownRows()
                    if (markdownRows.isNotBlank()) {
                        onSaveToMarkdown(markdownRows)
                        viewModel.clearSavedNamesAndText()
                    }
                },
                onUpdate = { viewModel.applySavedModalUpdate() },
                onChangeMdFile = onChangeMdFile,
                onDismiss = { viewModel.closeSavedModal() }
            )
        }

        // Combinations Modal
        if (uiState.isCombinationsModalOpen) {
            CombinationsModal(
                text = uiState.combinationsText,
                count = uiState.customCombinationsCount,
                onTextChange = { viewModel.updateCombinationsText(it) },
                onApply = { viewModel.applyCombinations() },
                onDismiss = { viewModel.closeCombinationsModal() }
            )
        }
    }
}

@Composable
private fun NameBoxItem(
    name: String,
    isSavedSuccess: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    index: Int
) {
    val checkColor by animateColorAsState(
        targetValue = if (isSavedSuccess) AccentGreen else Color.White.copy(alpha = 0.25f),
        animationSpec = tween(300),
        label = "checkColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NameBoxBg)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(TextPrimary),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(end = 36.dp)
                .testTag("name_input_$index")
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(checkColor)
                .clickable { onSave() }
                .testTag("save_btn_$index"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Save",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun OptionPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isSelected) PillSelectedBg else PillBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SavedNamesModal(
    text: String,
    isSaveToMd: Boolean,
    onTextChange: (String) -> Unit,
    onSaveToMd: () -> Unit,
    onUpdate: () -> Unit,
    onChangeMdFile: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Names",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .background(NameBoxBg, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BtnSecondaryBg)
                            .clickable { onChangeMdFile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Change .md", color = BtnSecondaryText, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSaveToMd) AccentGreen else BtnPrimaryBg)
                            .clickable { onSaveToMd() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isSaveToMd) "Saved!" else "Save to .md",
                            color = if (isSaveToMd) Color.White else BtnPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinationsModal(
    text: String,
    count: Int,
    onTextChange: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Combinations ($count)",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .background(NameBoxBg, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BtnPrimaryBg)
                        .clickable { onApply() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Apply", color = BtnPrimaryText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
