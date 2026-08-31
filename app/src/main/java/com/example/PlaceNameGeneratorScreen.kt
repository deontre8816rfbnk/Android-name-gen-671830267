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
import androidx.compose.foundation.layout.width
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

// Colors
private val BgLight = Color(0xFFF8F9FA)
private val CardBg = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF212529)
private val TextMuted = Color(0xFF6C757D)
private val BtnPrimaryBg = Color(0xFF212529)
private val BtnSecondaryBg = Color(0xFFE9ECEF)
private val BtnSecondaryText = Color(0xFF343A40)
private val PillBg = Color(0xFFE9ECEF)
private val PillSelectedBg = Color(0xFF343A40)
private val NameBoxBg = Color(0xFFF1F3F5)
private val BorderLight = Color(0xFFE9ECEF)
private val AccentGreen = Color(0xFF28A745)

@Composable
fun PlaceNameGeneratorScreen(
    viewModel: NameGeneratorViewModel,
    modifier: Modifier = Modifier,
    onSaveToMarkdown: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top bar - Saved count top right
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
                color = TextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                textAlign = TextAlign.Center
            )

            // Vertically stacked name boxes
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

            // BOTTOM CONTROLS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Horizontal scrollable style filters
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

                // Three buttons: +  |  Generate  |  View Saved
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + button for custom combinations
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
                            color = Color.White,
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
                    val markdown = viewModel.getSavedNamesAsMarkdownTable()
                    onSaveToMarkdown(markdown)
                    viewModel.markSaveToMdFeedback()
                },
                onUpdate = { viewModel.applySavedModalUpdate() },
                onDismiss = { viewModel.closeSavedModal() }
            )
        }

        // Custom Combinations Modal
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
        targetValue = if (isSavedSuccess) AccentGreen else Color(0xFF495057),
        animationSpec = tween(300),
        label = "checkColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NameBoxBg)
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            textStyle = TextStyle(
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(TextDark),
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
            color = if (isSelected) Color.White else TextMuted,
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
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Names",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(color = TextDark, fontSize = 15.sp, lineHeight = 22.sp),
                    cursorBrush = SolidColor(TextDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnPrimaryBg)
                            .clickable(onClick = onSaveToMd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSaveToMd) "Saved!" else "Save to .md",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnSecondaryBg)
                            .clickable(onClick = onUpdate),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Update List",
                            color = BtnSecondaryText,
                            fontSize = 14.sp,
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
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Letter Combinations",
                            color = TextDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$count active",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Add combinations (one per line or comma separated).\nThe generator will use them to create more unique names.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(color = TextDark, fontSize = 15.sp, lineHeight = 22.sp),
                    cursorBrush = SolidColor(TextDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BtnPrimaryBg)
                        .clickable(onClick = onApply),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Apply Combinations",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
