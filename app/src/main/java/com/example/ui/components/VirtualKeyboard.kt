package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothHidManager
import com.example.bluetooth.HidConsts
import com.example.keyboard.KeyDefinition
import com.example.keyboard.KeyType
import com.example.keyboard.KeyboardLayouts
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkKeyAction
import com.example.ui.theme.DarkKeyBg
import com.example.ui.theme.DarkKeyBgPressed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VirtualKeyboard(
    hidManager: BluetoothHidManager,
    isHidActive: Boolean,
    onOpenFullscreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isShiftActive by remember { mutableStateOf(false) }
    var isCtrlActive by remember { mutableStateOf(false) }
    var isAltActive by remember { mutableStateOf(false) }
    var isWinActive by remember { mutableStateOf(false) }
    var isFnActive by remember { mutableStateOf(false) }
    var isCapsLockActive by remember { mutableStateOf(false) }
    var isNumLockActive by remember { mutableStateOf(true) }

    var selectedClusterTab by remember { mutableStateOf(0) } // 0: All (Scrollable), 1: Main QWERTY, 2: Nav & Arrows, 3: Numpad

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("virtual_keyboard_container")
    ) {
        // Keyboard Header: Status LEDs and Cluster Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LED Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LedIndicator(label = "NUM", active = isNumLockActive)
                LedIndicator(label = "CAPS", active = isCapsLockActive)
                LedIndicator(label = "SCRL", active = false)
                LedIndicator(label = "HID", active = isHidActive, activeColor = GreenActive)
            }

            // Cluster Selector Chips + Fullscreen Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyboardTabChip("Full PC", selectedClusterTab == 0) { selectedClusterTab = 0 }
                KeyboardTabChip("Main", selectedClusterTab == 1) { selectedClusterTab = 1 }
                KeyboardTabChip("Nav/Arrows", selectedClusterTab == 2) { selectedClusterTab = 2 }
                KeyboardTabChip("Numpad", selectedClusterTab == 3) { selectedClusterTab = 3 }

                if (onOpenFullscreen != null) {
                    Surface(
                        onClick = onOpenFullscreen,
                        shape = RoundedCornerShape(6.dp),
                        color = CyanNeon.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Screen",
                                tint = CyanNeon,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "LANDSCAPE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Keyboard Body
        when (selectedClusterTab) {
            0 -> {
                // Full PC Keyboard: side-by-side clusters in horizontal scroll
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MainQwertyCluster(
                        isShiftActive = isShiftActive,
                        isCapsLockActive = isCapsLockActive,
                        isCtrlActive = isCtrlActive,
                        isAltActive = isAltActive,
                        isWinActive = isWinActive,
                        isFnActive = isFnActive,
                        onModifierToggle = { mask ->
                            when (mask) {
                                HidConsts.MOD_LEFT_SHIFT, HidConsts.MOD_RIGHT_SHIFT -> {
                                    isShiftActive = !isShiftActive
                                    hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, isShiftActive)
                                }
                                HidConsts.MOD_LEFT_CTRL, HidConsts.MOD_RIGHT_CTRL -> {
                                    isCtrlActive = !isCtrlActive
                                    hidManager.setModifier(HidConsts.MOD_LEFT_CTRL, isCtrlActive)
                                }
                                HidConsts.MOD_LEFT_ALT, HidConsts.MOD_RIGHT_ALT -> {
                                    isAltActive = !isAltActive
                                    hidManager.setModifier(HidConsts.MOD_LEFT_ALT, isAltActive)
                                }
                                HidConsts.MOD_LEFT_GUI -> {
                                    isWinActive = !isWinActive
                                    hidManager.setModifier(HidConsts.MOD_LEFT_GUI, isWinActive)
                                }
                            }
                        },
                        onKeyAction = { key ->
                            handleKeyAction(
                                key = key,
                                hidManager = hidManager,
                                isShiftActive = isShiftActive,
                                isCapsLockActive = isCapsLockActive,
                                onCapsToggle = { isCapsLockActive = !isCapsLockActive },
                                onFnToggle = { isFnActive = !isFnActive },
                                onShiftConsumed = {
                                    if (isShiftActive) {
                                        isShiftActive = false
                                        hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, false)
                                    }
                                }
                            )
                        }
                    )

                    NavAndArrowCluster(
                        onKeyAction = { key ->
                            handleKeyAction(
                                key = key,
                                hidManager = hidManager,
                                isShiftActive = isShiftActive,
                                isCapsLockActive = isCapsLockActive,
                                onCapsToggle = {},
                                onFnToggle = {},
                                onShiftConsumed = {}
                            )
                        }
                    )

                    NumpadCluster(
                        isNumLockActive = isNumLockActive,
                        onNumLockToggle = {
                            isNumLockActive = !isNumLockActive
                            hidManager.sendKeyPress(HidConsts.KEY_NUMLOCK)
                        },
                        onKeyAction = { key ->
                            handleKeyAction(
                                key = key,
                                hidManager = hidManager,
                                isShiftActive = isShiftActive,
                                isCapsLockActive = isCapsLockActive,
                                onCapsToggle = {},
                                onFnToggle = {},
                                onShiftConsumed = {}
                            )
                        }
                    )
                }
            }
            1 -> {
                // Main QWERTY cluster (scaled to width)
                MainQwertyCluster(
                    isShiftActive = isShiftActive,
                    isCapsLockActive = isCapsLockActive,
                    isCtrlActive = isCtrlActive,
                    isAltActive = isAltActive,
                    isWinActive = isWinActive,
                    isFnActive = isFnActive,
                    onModifierToggle = { mask ->
                        when (mask) {
                            HidConsts.MOD_LEFT_SHIFT, HidConsts.MOD_RIGHT_SHIFT -> {
                                isShiftActive = !isShiftActive
                                hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, isShiftActive)
                            }
                            HidConsts.MOD_LEFT_CTRL, HidConsts.MOD_RIGHT_CTRL -> {
                                isCtrlActive = !isCtrlActive
                                hidManager.setModifier(HidConsts.MOD_LEFT_CTRL, isCtrlActive)
                            }
                            HidConsts.MOD_LEFT_ALT, HidConsts.MOD_RIGHT_ALT -> {
                                isAltActive = !isAltActive
                                hidManager.setModifier(HidConsts.MOD_LEFT_ALT, isAltActive)
                            }
                            HidConsts.MOD_LEFT_GUI -> {
                                isWinActive = !isWinActive
                                hidManager.setModifier(HidConsts.MOD_LEFT_GUI, isWinActive)
                            }
                        }
                    },
                    onKeyAction = { key ->
                        handleKeyAction(
                            key = key,
                            hidManager = hidManager,
                            isShiftActive = isShiftActive,
                            isCapsLockActive = isCapsLockActive,
                            onCapsToggle = { isCapsLockActive = !isCapsLockActive },
                            onFnToggle = { isFnActive = !isFnActive },
                            onShiftConsumed = {
                                if (isShiftActive) {
                                    isShiftActive = false
                                    hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, false)
                                }
                            }
                        )
                    }
                )
            }
            2 -> {
                NavAndArrowCluster(
                    modifier = Modifier.fillMaxWidth(),
                    onKeyAction = { key ->
                        handleKeyAction(
                            key = key,
                            hidManager = hidManager,
                            isShiftActive = isShiftActive,
                            isCapsLockActive = isCapsLockActive,
                            onCapsToggle = {},
                            onFnToggle = {},
                            onShiftConsumed = {}
                        )
                    }
                )
            }
            3 -> {
                NumpadCluster(
                    modifier = Modifier.fillMaxWidth(),
                    isNumLockActive = isNumLockActive,
                    onNumLockToggle = {
                        isNumLockActive = !isNumLockActive
                        hidManager.sendKeyPress(HidConsts.KEY_NUMLOCK)
                    },
                    onKeyAction = { key ->
                        handleKeyAction(
                            key = key,
                            hidManager = hidManager,
                            isShiftActive = isShiftActive,
                            isCapsLockActive = isCapsLockActive,
                            onCapsToggle = {},
                            onFnToggle = {},
                            onShiftConsumed = {}
                        )
                    }
                )
            }
        }
    }
}

private fun handleKeyAction(
    key: KeyDefinition,
    hidManager: BluetoothHidManager,
    isShiftActive: Boolean,
    isCapsLockActive: Boolean,
    onCapsToggle: () -> Unit,
    onFnToggle: () -> Unit,
    onShiftConsumed: () -> Unit
) {
    when (key.id) {
        "CAPS" -> {
            onCapsToggle()
            hidManager.sendKeyPress(HidConsts.KEY_CAPSLOCK)
        }
        "FN", "FN2" -> {
            onFnToggle()
        }
        else -> {
            if (key.usageCode != HidConsts.KEY_NONE) {
                hidManager.sendKeyPress(key.usageCode)
                if (isShiftActive && key.keyType == KeyType.NORMAL) {
                    onShiftConsumed()
                }
            }
        }
    }
}

@Composable
fun MainQwertyCluster(
    isShiftActive: Boolean,
    isCapsLockActive: Boolean,
    isCtrlActive: Boolean,
    isAltActive: Boolean,
    isWinActive: Boolean,
    isFnActive: Boolean,
    onModifierToggle: (Byte) -> Unit,
    onKeyAction: (KeyDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(min = 680.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Function row
        KeyRow(
            keys = KeyboardLayouts.FUNCTION_ROW,
            isShift = isShiftActive,
            isCaps = isCapsLockActive,
            onModifierToggle = onModifierToggle,
            onKeyAction = onKeyAction,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isShiftActive = isShiftActive
        )
        // Number row
        KeyRow(
            keys = KeyboardLayouts.ROW_NUMBER,
            isShift = isShiftActive,
            isCaps = isCapsLockActive,
            onModifierToggle = onModifierToggle,
            onKeyAction = onKeyAction,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isShiftActive = isShiftActive
        )
        // QWERTY row
        KeyRow(
            keys = KeyboardLayouts.ROW_QWERTY,
            isShift = isShiftActive,
            isCaps = isCapsLockActive,
            onModifierToggle = onModifierToggle,
            onKeyAction = onKeyAction,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isShiftActive = isShiftActive
        )
        // Home row (Caps, A-L, Enter)
        KeyRow(
            keys = KeyboardLayouts.ROW_HOME,
            isShift = isShiftActive,
            isCaps = isCapsLockActive,
            onModifierToggle = onModifierToggle,
            onKeyAction = onKeyAction,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isShiftActive = isShiftActive
        )
        // Bottom row (Shift, Z-M, Shift)
        KeyRow(
            keys = KeyboardLayouts.ROW_BOTTOM,
            isShift = isShiftActive,
            isCaps = isCapsLockActive,
            onModifierToggle = onModifierToggle,
            onKeyAction = onKeyAction,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isShiftActive = isShiftActive
        )
        // Space / Modifier row
        KeyRow(
            keys = KeyboardLayouts.ROW_SPACE,
            isShift = isShiftActive,
            isCaps = isCapsLockActive,
            onModifierToggle = onModifierToggle,
            onKeyAction = onKeyAction,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isShiftActive = isShiftActive
        )
    }
}

@Composable
fun NavAndArrowCluster(
    onKeyAction: (KeyDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(min = 140.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Nav block
        KeyRow(keys = KeyboardLayouts.NAV_ROW_1, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
        KeyRow(keys = KeyboardLayouts.NAV_ROW_2, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)

        Spacer(modifier = Modifier.height(16.dp))

        // Arrow block
        KeyRow(keys = KeyboardLayouts.ARROW_ROW_1, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
        KeyRow(keys = KeyboardLayouts.ARROW_ROW_2, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
    }
}

@Composable
fun NumpadCluster(
    isNumLockActive: Boolean,
    onNumLockToggle: () -> Unit,
    onKeyAction: (KeyDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(min = 180.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyRow(
            keys = KeyboardLayouts.NUMPAD_ROW_0,
            isShift = false,
            isCaps = false,
            onModifierToggle = { onNumLockToggle() },
            onKeyAction = { key ->
                if (key.id == "NUMLOCK") onNumLockToggle() else onKeyAction(key)
            }
        )
        KeyRow(keys = KeyboardLayouts.NUMPAD_ROW_1, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
        KeyRow(keys = KeyboardLayouts.NUMPAD_ROW_2, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
        KeyRow(keys = KeyboardLayouts.NUMPAD_ROW_3, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
        KeyRow(keys = KeyboardLayouts.NUMPAD_ROW_4, isShift = false, isCaps = false, onModifierToggle = {}, onKeyAction = onKeyAction)
    }
}

@Composable
fun KeyRow(
    keys: List<KeyDefinition>,
    isShift: Boolean,
    isCaps: Boolean,
    onModifierToggle: (Byte) -> Unit,
    onKeyAction: (KeyDefinition) -> Unit,
    isCtrlActive: Boolean = false,
    isAltActive: Boolean = false,
    isWinActive: Boolean = false,
    isShiftActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (key in keys) {
            if (key.id.startsWith("SPACER")) {
                if (key.weight > 0) {
                    Spacer(modifier = Modifier.weight(key.weight))
                }
                continue
            }

            val isModifierActive = when {
                key.id == "LSHIFT" || key.id == "RSHIFT" -> isShiftActive
                key.id == "LCTRL" || key.id == "RCTRL" -> isCtrlActive
                key.id == "LALT" || key.id == "RALT" -> isAltActive
                key.id == "WIN" -> isWinActive
                key.id == "CAPS" -> isCaps
                else -> false
            }

            VirtualKey(
                keyDef = key,
                isShift = isShift,
                isCaps = isCaps,
                isModifierActive = isModifierActive,
                onClick = {
                    if (key.modifierMask != 0.toByte()) {
                        onModifierToggle(key.modifierMask)
                    } else {
                        onKeyAction(key)
                    }
                },
                modifier = Modifier.weight(key.weight)
            )
        }
    }
}

@Composable
fun VirtualKey(
    keyDef: KeyDefinition,
    isShift: Boolean,
    isCaps: Boolean,
    isModifierActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isPressedState by remember { mutableStateOf(false) }

    val baseBgColor = when {
        isModifierActive -> CyanNeon.copy(alpha = 0.25f)
        keyDef.keyType == KeyType.ACTION -> DarkKeyAction
        keyDef.keyType == KeyType.FUNCTION -> DarkSurfaceElevated
        keyDef.keyType == KeyType.MODIFIER -> DarkKeyAction
        keyDef.keyType == KeyType.ARROW -> DarkKeyBg
        else -> DarkKeyBg
    }

    val targetBgColor = if (isPressedState) DarkKeyBgPressed else baseBgColor
    val animatedBg by animateColorAsState(targetValue = targetBgColor, label = "key_bg")

    val borderColor = if (isModifierActive) CyanNeon else DarkBorder

    // Determine displayed label
    val displayPrimary = when {
        keyDef.keyType == KeyType.NORMAL && keyDef.primaryLabel.length == 1 && keyDef.primaryLabel[0].isLetter() -> {
            val upper = isShift xor isCaps
            if (upper) keyDef.primaryLabel.uppercase() else keyDef.primaryLabel.lowercase()
        }
        else -> keyDef.primaryLabel
    }

    val displayShift = if (isShift && keyDef.shiftLabel != null) null else keyDef.shiftLabel
    val primaryText = if (isShift && keyDef.shiftLabel != null) keyDef.shiftLabel else displayPrimary

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(animatedBg)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .pointerInput(keyDef.id) {
                detectTapGestures(
                    onPress = {
                        isPressedState = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressedState = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .testTag("key_${keyDef.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            if (displayShift != null) {
                Text(
                    text = displayShift,
                    fontSize = 9.sp,
                    color = TextSecondary,
                    lineHeight = 10.sp
                )
            }
            Text(
                text = primaryText,
                fontSize = when {
                    primaryText.length > 4 -> 10.sp
                    primaryText.length > 2 -> 11.sp
                    else -> 13.sp
                },
                fontWeight = if (keyDef.keyType == KeyType.NORMAL) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isModifierActive) CyanNeon else TextPrimary,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LedIndicator(label: String, active: Boolean, activeColor: Color = AmberWarning) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (active) activeColor else TextMuted.copy(alpha = 0.4f))
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) activeColor else TextMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun KeyboardTabChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceElevated,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanNeon) else null,
        modifier = Modifier.height(26.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) CyanNeon else TextSecondary
            )
        }
    }
}
