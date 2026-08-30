package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BgColor
import com.example.ui.theme.BtnPrimaryBg
import com.example.ui.theme.BtnSecondaryBg
import com.example.ui.theme.BtnSecondaryText
import com.example.ui.theme.CardBg
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ModalBg
import com.example.ui.theme.ModalClose
import com.example.ui.theme.OptionActiveBg
import com.example.ui.theme.OptionInactiveBg
import com.example.ui.theme.SaveBtnBg
import com.example.ui.theme.SaveBtnSuccessBg
import com.example.ui.theme.TextAreaBg
import com.example.ui.theme.TextAreaBorder
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOptionInactive
import com.example.ui.theme.TitleColor

@Composable
fun PlaceNameGeneratorScreen(
    viewModel: NameGeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Title
            Text(
                text = stringResource(R.string.title_advanced_place_name_generator),
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleColor,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_title")
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 2x2 Grid of Name Boxes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NameBoxItem(
                        name = uiState.slotNames.getOrElse(0) { "" },
                        isSavedSuccess = 0 in uiState.savedSlotsFeedback,
                        onNameChange = { viewModel.updateSlotName(0, it) },
                        onSave = { viewModel.saveSlot(0) },
                        index = 0,
                        modifier = Modifier.weight(1f)
                    )
                    NameBoxItem(
                        name = uiState.slotNames.getOrElse(1) { "" },
                        isSavedSuccess = 1 in uiState.savedSlotsFeedback,
                        onNameChange = { viewModel.updateSlotName(1, it) },
                        onSave = { viewModel.saveSlot(1) },
                        index = 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NameBoxItem(
                        name = uiState.slotNames.getOrElse(2) { "" },
                        isSavedSuccess = 2 in uiState.savedSlotsFeedback,
                        onNameChange = { viewModel.updateSlotName(2, it) },
                        onSave = { viewModel.saveSlot(2) },
                        index = 2,
                        modifier = Modifier.weight(1f)
                    )
                    NameBoxItem(
                        name = uiState.slotNames.getOrElse(3) { "" },
                        isSavedSuccess = 3 in uiState.savedSlotsFeedback,
                        onNameChange = { viewModel.updateSlotName(3, it) },
                        onSave = { viewModel.saveSlot(3) },
                        index = 3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Style Option Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NameStyle.entries.forEachIndexed { index, style ->
                    val isSelected = uiState.selectedStyle == style
                    OptionPill(
                        style = style,
                        isSelected = isSelected,
                        onClick = { viewModel.onSelectStyle(style) },
                        modifier = Modifier.testTag("option_btn_${style.key}")
                    )
                    if (index < NameStyle.entries.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Generate Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BtnPrimaryBg)
                        .clickable {
                            focusManager.clearFocus()
                            viewModel.generateNames()
                        }
                        .testTag("generate_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_generate_names),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // View Saved Names Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BtnSecondaryBg)
                        .clickable {
                            focusManager.clearFocus()
                            viewModel.openSavedModal()
                        }
                        .testTag("view_saved_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_view_saved),
                        color = BtnSecondaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Saved count badge
            Box(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(9999.dp))
                    .background(Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(9999.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(9999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("saved_count_badge")
            ) {
                Text(
                    text = stringResource(R.string.saved_count_format, uiState.savedNames.size),
                    color = TextMuted,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Saved Names Modal Dialog
        if (uiState.isSavedModalOpen) {
            SavedNamesDialog(
                savedText = uiState.savedModalText,
                isCopied = uiState.isCopiedFeedback,
                onTextChange = { viewModel.updateSavedModalText(it) },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(uiState.savedModalText))
                    viewModel.markCopiedFeedback()
                },
                onUpdate = { viewModel.applySavedModalUpdate() },
                onDismiss = { viewModel.closeSavedModal() }
            )
        }
    }
}

@Composable
fun NameBoxItem(
    name: String,
    isSavedSuccess: Boolean,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val saveBtnBg by animateColorAsState(
        targetValue = if (isSavedSuccess) SaveBtnSuccessBg else SaveBtnBg,
        animationSpec = tween(durationMillis = 200),
        label = "saveBtnBg"
    )

    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
            .background(CardBg, shape = RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 18.dp)
            .testTag("name_box_$index")
    ) {
        // Name Text Input (Centered, borderless)
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            textStyle = TextStyle(
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                textAlign = TextAlign.Center,
                letterSpacing = 0.2.sp
            ),
            cursorBrush = SolidColor(TextDark),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {}),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .align(Alignment.Center)
                .testTag("name_input_$index")
        )

        // Save Button (Top Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(saveBtnBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSave
                )
                .testTag("save_btn_$index"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.save_name_desc),
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun OptionPill(
    style: NameStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) OptionActiveBg else OptionInactiveBg
    val textColor = if (isSelected) Color.White else TextOptionInactive

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = style.displayName,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SavedNamesDialog(
    savedText: String,
    isCopied: Boolean,
    onTextChange: (String) -> Unit,
    onCopy: () -> Unit,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ModalBg,
            shadowElevation = 16.dp,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .testTag("saved_names_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with title and close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.saved_names_title),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_modal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = ModalClose,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Area for saved names
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 280.dp)
                        .background(TextAreaBg, shape = RoundedCornerShape(14.dp))
                        .border(1.dp, TextAreaBorder, shape = RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = savedText,
                        onValueChange = onTextChange,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = TextDark,
                            fontFamily = FontFamily.Default
                        ),
                        cursorBrush = SolidColor(TextDark),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("saved_names_text")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Copy and Update List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy Button
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnSecondaryBg)
                            .clickable(onClick = onCopy)
                            .padding(horizontal = 22.dp)
                            .testTag("copy_saved_names_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCopied) stringResource(R.string.btn_copied) else stringResource(R.string.btn_copy),
                            color = BtnSecondaryText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Update List Button
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BtnPrimaryBg)
                            .clickable(onClick = onUpdate)
                            .padding(horizontal = 22.dp)
                            .testTag("update_saved_names_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.btn_update_list),
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
