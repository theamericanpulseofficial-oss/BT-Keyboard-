package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothHidManager
import com.example.bluetooth.HidConsts
import com.example.keyboard.KeyDefinition
import com.example.keyboard.KeyType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkKeyAction
import com.example.ui.theme.DarkKeyBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Clean, lightweight, 1-piece Laptop Keyboard layout (QWERTY + Compact Function & Arrows).
 * Optimized for low-end (1GB RAM) Android devices with zero recomposition lag.
 */
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
    var isCapsLockActive by remember { mutableStateOf(false) }
    var isFnActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("virtual_keyboard_container")
    ) {
        // Keyboard Header: Status Bar & LEDs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status LEDs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAPTOP KEYBOARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                LedDot(label = "CAPS", active = isCapsLockActive)
                LedDot(label = "SHIFT", active = isShiftActive)
                LedDot(label = "CTRL", active = isCtrlActive)
                LedDot(label = "HID", active = isHidActive, activeColor = GreenActive)
            }

            if (onOpenFullscreen != null) {
                Surface(
                    onClick = onOpenFullscreen,
                    shape = RoundedCornerShape(6.dp),
                    color = CyanNeon.copy(alpha = 0.15f),
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
                            modifier = Modifier.size(13.dp)
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

        Spacer(modifier = Modifier.height(4.dp))

        // Single Laptop Keyboard - Rows rendered cleanly without horizontal scroll
        val keyAction: (KeyDefinition) -> Unit = { key ->
            when (key.id) {
                "CAPS" -> {
                    isCapsLockActive = !isCapsLockActive
                    hidManager.sendKeyPress(HidConsts.KEY_CAPSLOCK)
                }
                "FN" -> {
                    isFnActive = !isFnActive
                }
                "LSHIFT", "RSHIFT" -> {
                    isShiftActive = !isShiftActive
                    hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, isShiftActive)
                }
                "LCTRL", "RCTRL" -> {
                    isCtrlActive = !isCtrlActive
                    hidManager.setModifier(HidConsts.MOD_LEFT_CTRL, isCtrlActive)
                }
                "LALT", "RALT" -> {
                    isAltActive = !isAltActive
                    hidManager.setModifier(HidConsts.MOD_LEFT_ALT, isAltActive)
                }
                "WIN" -> {
                    isWinActive = !isWinActive
                    hidManager.setModifier(HidConsts.MOD_LEFT_GUI, isWinActive)
                }
                else -> {
                    if (key.usageCode != HidConsts.KEY_NONE) {
                        hidManager.sendKeyPress(key.usageCode)
                        if (isShiftActive && key.keyType == KeyType.NORMAL) {
                            isShiftActive = false
                            hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, false)
                        }
                    }
                }
            }
        }

        // Row 0: Function Row (Esc, F1-F12, Del)
        LaptopKeyRow(
            keys = if (isFnActive) LAPTOP_FN_ROW else LAPTOP_F_KEYS_ROW,
            isShiftActive = isShiftActive,
            isCapsLockActive = isCapsLockActive,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isFnActive = isFnActive,
            onKeyAction = keyAction,
            heightDp = 28
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Row 1: Numbers (~, 1-0, -, =, Backspace)
        LaptopKeyRow(
            keys = LAPTOP_NUMBER_ROW,
            isShiftActive = isShiftActive,
            isCapsLockActive = isCapsLockActive,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isFnActive = isFnActive,
            onKeyAction = keyAction,
            heightDp = 34
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Row 2: Tab, QWERTYUIOP, [, ], \
        LaptopKeyRow(
            keys = LAPTOP_QWERTY_ROW,
            isShiftActive = isShiftActive,
            isCapsLockActive = isCapsLockActive,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isFnActive = isFnActive,
            onKeyAction = keyAction,
            heightDp = 34
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Row 3: Caps, ASDFGHJKL, ;, ', Enter
        LaptopKeyRow(
            keys = LAPTOP_HOME_ROW,
            isShiftActive = isShiftActive,
            isCapsLockActive = isCapsLockActive,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isFnActive = isFnActive,
            onKeyAction = keyAction,
            heightDp = 34
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Row 4: Shift, ZXCVBNM, ,, ., /, ▲, Shift
        LaptopKeyRow(
            keys = LAPTOP_BOTTOM_ROW,
            isShiftActive = isShiftActive,
            isCapsLockActive = isCapsLockActive,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isFnActive = isFnActive,
            onKeyAction = keyAction,
            heightDp = 34
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Row 5: Ctrl, Fn, Win, Alt, Space, Alt, ◄, ▼, ►
        LaptopKeyRow(
            keys = LAPTOP_SPACE_ROW,
            isShiftActive = isShiftActive,
            isCapsLockActive = isCapsLockActive,
            isCtrlActive = isCtrlActive,
            isAltActive = isAltActive,
            isWinActive = isWinActive,
            isFnActive = isFnActive,
            onKeyAction = keyAction,
            heightDp = 34
        )
    }
}

@Composable
fun LaptopKeyRow(
    keys: List<KeyDefinition>,
    isShiftActive: Boolean,
    isCapsLockActive: Boolean,
    isCtrlActive: Boolean,
    isAltActive: Boolean,
    isWinActive: Boolean,
    isFnActive: Boolean,
    onKeyAction: (KeyDefinition) -> Unit,
    heightDp: Int = 34
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        keys.forEach { key ->
            val isModifierOn = when (key.id) {
                "LSHIFT", "RSHIFT" -> isShiftActive
                "LCTRL", "RCTRL" -> isCtrlActive
                "LALT", "RALT" -> isAltActive
                "WIN" -> isWinActive
                "CAPS" -> isCapsLockActive
                "FN" -> isFnActive
                else -> false
            }

            LaptopKeyButton(
                key = key,
                isShiftActive = isShiftActive,
                isCapsLockActive = isCapsLockActive,
                isModifierOn = isModifierOn,
                onClick = { onKeyAction(key) },
                modifier = Modifier
                    .weight(key.weight)
                    .height(heightDp.dp)
            )
        }
    }
}

@Composable
fun LaptopKeyButton(
    key: KeyDefinition,
    isShiftActive: Boolean,
    isCapsLockActive: Boolean,
    isModifierOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val displayLabel = when {
        isShiftActive && key.shiftLabel != null -> key.shiftLabel
        (isShiftActive || isCapsLockActive) && key.keyType == KeyType.NORMAL && key.primaryLabel.length == 1 -> {
            key.primaryLabel.uppercase()
        }
        key.keyType == KeyType.NORMAL && key.primaryLabel.length == 1 -> {
            key.primaryLabel.lowercase()
        }
        else -> key.primaryLabel
    }

    val backgroundColor = when {
        isPressed -> CyanNeon.copy(alpha = 0.35f)
        isModifierOn -> BlueElectric.copy(alpha = 0.35f)
        key.keyType == KeyType.MODIFIER || key.keyType == KeyType.ACTION -> DarkKeyAction
        key.keyType == KeyType.FUNCTION -> DarkSurfaceElevated
        else -> DarkKeyBg
    }

    val borderColor = when {
        isPressed -> CyanNeon
        isModifierOn -> BlueElectric
        else -> DarkBorder
    }

    val textColor = when {
        isPressed -> CyanNeon
        isModifierOn -> CyanNeon
        key.keyType == KeyType.ACTION -> Color(0xFFE2E8F0)
        key.keyType == KeyType.ARROW -> CyanNeon
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .pointerInput(key.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .testTag("key_${key.id}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayLabel,
            fontSize = if (displayLabel.length > 3) 9.sp else if (displayLabel.length > 1) 10.sp else 12.sp,
            fontWeight = if (key.keyType == KeyType.NORMAL) FontWeight.Medium else FontWeight.Bold,
            color = textColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun LedDot(
    label: String,
    active: Boolean,
    activeColor: Color = AmberWarning
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (active) activeColor else TextMuted.copy(alpha = 0.4f))
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) activeColor else TextMuted
        )
    }
}

// -------------------------------------------------------------
// LAPTOP COMPACT KEYBOARD LAYOUT DEFINITION (Fits perfectly on screen)
// -------------------------------------------------------------

val LAPTOP_F_KEYS_ROW = listOf(
    KeyDefinition("ESC", "Esc", usageCode = HidConsts.KEY_ESC, keyType = KeyType.ACTION, weight = 1.1f),
    KeyDefinition("F1", "F1", usageCode = HidConsts.KEY_F1, keyType = KeyType.FUNCTION),
    KeyDefinition("F2", "F2", usageCode = HidConsts.KEY_F2, keyType = KeyType.FUNCTION),
    KeyDefinition("F3", "F3", usageCode = HidConsts.KEY_F3, keyType = KeyType.FUNCTION),
    KeyDefinition("F4", "F4", usageCode = HidConsts.KEY_F4, keyType = KeyType.FUNCTION),
    KeyDefinition("F5", "F5", usageCode = HidConsts.KEY_F5, keyType = KeyType.FUNCTION),
    KeyDefinition("F6", "F6", usageCode = HidConsts.KEY_F6, keyType = KeyType.FUNCTION),
    KeyDefinition("F7", "F7", usageCode = HidConsts.KEY_F7, keyType = KeyType.FUNCTION),
    KeyDefinition("F8", "F8", usageCode = HidConsts.KEY_F8, keyType = KeyType.FUNCTION),
    KeyDefinition("F9", "F9", usageCode = HidConsts.KEY_F9, keyType = KeyType.FUNCTION),
    KeyDefinition("F10", "F10", usageCode = HidConsts.KEY_F10, keyType = KeyType.FUNCTION),
    KeyDefinition("F11", "F11", usageCode = HidConsts.KEY_F11, keyType = KeyType.FUNCTION),
    KeyDefinition("F12", "F12", usageCode = HidConsts.KEY_F12, keyType = KeyType.FUNCTION),
    KeyDefinition("DEL", "Del", usageCode = HidConsts.KEY_DELETE, keyType = KeyType.ACTION, weight = 1.1f)
)

val LAPTOP_FN_ROW = listOf(
    KeyDefinition("ESC", "Esc", usageCode = HidConsts.KEY_ESC, keyType = KeyType.ACTION, weight = 1.1f),
    KeyDefinition("F1", "F1", usageCode = HidConsts.KEY_F1, keyType = KeyType.FUNCTION),
    KeyDefinition("F2", "F2", usageCode = HidConsts.KEY_F2, keyType = KeyType.FUNCTION),
    KeyDefinition("F3", "F3", usageCode = HidConsts.KEY_F3, keyType = KeyType.FUNCTION),
    KeyDefinition("F4", "F4", usageCode = HidConsts.KEY_F4, keyType = KeyType.FUNCTION),
    KeyDefinition("F5", "F5", usageCode = HidConsts.KEY_F5, keyType = KeyType.FUNCTION),
    KeyDefinition("F6", "F6", usageCode = HidConsts.KEY_F6, keyType = KeyType.FUNCTION),
    KeyDefinition("HOME", "Home", usageCode = HidConsts.KEY_HOME, keyType = KeyType.FUNCTION),
    KeyDefinition("END", "End", usageCode = HidConsts.KEY_END, keyType = KeyType.FUNCTION),
    KeyDefinition("PGUP", "PgUp", usageCode = HidConsts.KEY_PAGEUP, keyType = KeyType.FUNCTION),
    KeyDefinition("PGDN", "PgDn", usageCode = HidConsts.KEY_PAGEDOWN, keyType = KeyType.FUNCTION),
    KeyDefinition("PRTSC", "Prt", usageCode = HidConsts.KEY_PRINTSCREEN, keyType = KeyType.FUNCTION),
    KeyDefinition("INS", "Ins", usageCode = HidConsts.KEY_INSERT, keyType = KeyType.FUNCTION),
    KeyDefinition("DEL", "Del", usageCode = HidConsts.KEY_DELETE, keyType = KeyType.ACTION, weight = 1.1f)
)

val LAPTOP_NUMBER_ROW = listOf(
    KeyDefinition("GRAVE", "`", "~", HidConsts.KEY_GRAVE, weight = 0.9f),
    KeyDefinition("1", "1", "!", HidConsts.KEY_1),
    KeyDefinition("2", "2", "@", HidConsts.KEY_2),
    KeyDefinition("3", "3", "#", HidConsts.KEY_3),
    KeyDefinition("4", "4", "$", HidConsts.KEY_4),
    KeyDefinition("5", "5", "%", HidConsts.KEY_5),
    KeyDefinition("6", "6", "^", HidConsts.KEY_6),
    KeyDefinition("7", "7", "&", HidConsts.KEY_7),
    KeyDefinition("8", "8", "*", HidConsts.KEY_8),
    KeyDefinition("9", "9", "(", HidConsts.KEY_9),
    KeyDefinition("0", "0", ")", HidConsts.KEY_0),
    KeyDefinition("MINUS", "-", "_", HidConsts.KEY_MINUS),
    KeyDefinition("EQUAL", "=", "+", HidConsts.KEY_EQUAL),
    KeyDefinition("BACKSPACE", "⌫", usageCode = HidConsts.KEY_BACKSPACE, keyType = KeyType.ACTION, weight = 1.5f)
)

val LAPTOP_QWERTY_ROW = listOf(
    KeyDefinition("TAB", "Tab", usageCode = HidConsts.KEY_TAB, keyType = KeyType.ACTION, weight = 1.3f),
    KeyDefinition("Q", "Q", usageCode = HidConsts.KEY_Q),
    KeyDefinition("W", "W", usageCode = HidConsts.KEY_W),
    KeyDefinition("E", "E", usageCode = HidConsts.KEY_E),
    KeyDefinition("R", "R", usageCode = HidConsts.KEY_R),
    KeyDefinition("T", "T", usageCode = HidConsts.KEY_T),
    KeyDefinition("Y", "Y", usageCode = HidConsts.KEY_Y),
    KeyDefinition("U", "U", usageCode = HidConsts.KEY_U),
    KeyDefinition("I", "I", usageCode = HidConsts.KEY_I),
    KeyDefinition("O", "O", usageCode = HidConsts.KEY_O),
    KeyDefinition("P", "P", usageCode = HidConsts.KEY_P),
    KeyDefinition("LBRACKET", "[", "{", HidConsts.KEY_LEFTBRACE, weight = 0.9f),
    KeyDefinition("RBRACKET", "]", "}", HidConsts.KEY_RIGHTBRACE, weight = 0.9f),
    KeyDefinition("BACKSLASH", "\\", "|", HidConsts.KEY_BACKSLASH, weight = 1.1f)
)

val LAPTOP_HOME_ROW = listOf(
    KeyDefinition("CAPS", "Caps", usageCode = HidConsts.KEY_CAPSLOCK, keyType = KeyType.MODIFIER, weight = 1.5f),
    KeyDefinition("A", "A", usageCode = HidConsts.KEY_A),
    KeyDefinition("S", "S", usageCode = HidConsts.KEY_S),
    KeyDefinition("D", "D", usageCode = HidConsts.KEY_D),
    KeyDefinition("F", "F", usageCode = HidConsts.KEY_F),
    KeyDefinition("G", "G", usageCode = HidConsts.KEY_G),
    KeyDefinition("H", "H", usageCode = HidConsts.KEY_H),
    KeyDefinition("J", "J", usageCode = HidConsts.KEY_J),
    KeyDefinition("K", "K", usageCode = HidConsts.KEY_K),
    KeyDefinition("L", "L", usageCode = HidConsts.KEY_L),
    KeyDefinition("SEMICOLON", ";", ":", HidConsts.KEY_SEMICOLON, weight = 0.9f),
    KeyDefinition("QUOTE", "'", "\"", HidConsts.KEY_APOSTROPHE, weight = 0.9f),
    KeyDefinition("ENTER", "Enter", usageCode = HidConsts.KEY_ENTER, keyType = KeyType.ACTION, weight = 1.8f)
)

val LAPTOP_BOTTOM_ROW = listOf(
    KeyDefinition("LSHIFT", "Shift", modifierMask = HidConsts.MOD_LEFT_SHIFT, keyType = KeyType.MODIFIER, weight = 1.8f),
    KeyDefinition("Z", "Z", usageCode = HidConsts.KEY_Z),
    KeyDefinition("X", "X", usageCode = HidConsts.KEY_X),
    KeyDefinition("C", "C", usageCode = HidConsts.KEY_C),
    KeyDefinition("V", "V", usageCode = HidConsts.KEY_V),
    KeyDefinition("B", "B", usageCode = HidConsts.KEY_B),
    KeyDefinition("N", "N", usageCode = HidConsts.KEY_N),
    KeyDefinition("M", "M", usageCode = HidConsts.KEY_M),
    KeyDefinition("COMMA", ",", "<", HidConsts.KEY_COMMA, weight = 0.9f),
    KeyDefinition("DOT", ".", ">", HidConsts.KEY_DOT, weight = 0.9f),
    KeyDefinition("SLASH", "/", "?", HidConsts.KEY_SLASH, weight = 0.9f),
    KeyDefinition("UP", "▲", usageCode = HidConsts.KEY_UP, keyType = KeyType.ARROW, weight = 1.0f),
    KeyDefinition("RSHIFT", "Shift", modifierMask = HidConsts.MOD_RIGHT_SHIFT, keyType = KeyType.MODIFIER, weight = 1.3f)
)

val LAPTOP_SPACE_ROW = listOf(
    KeyDefinition("LCTRL", "Ctrl", modifierMask = HidConsts.MOD_LEFT_CTRL, keyType = KeyType.MODIFIER, weight = 1.2f),
    KeyDefinition("FN", "Fn", keyType = KeyType.MODIFIER, weight = 1.0f),
    KeyDefinition("WIN", "⊞", modifierMask = HidConsts.MOD_LEFT_GUI, keyType = KeyType.MODIFIER, weight = 1.0f),
    KeyDefinition("LALT", "Alt", modifierMask = HidConsts.MOD_LEFT_ALT, keyType = KeyType.MODIFIER, weight = 1.1f),
    KeyDefinition("SPACE", "Space", usageCode = HidConsts.KEY_SPACE, keyType = KeyType.NORMAL, weight = 4.2f),
    KeyDefinition("RALT", "Alt", modifierMask = HidConsts.MOD_RIGHT_ALT, keyType = KeyType.MODIFIER, weight = 1.1f),
    KeyDefinition("LEFT", "◄", usageCode = HidConsts.KEY_LEFT, keyType = KeyType.ARROW, weight = 1.0f),
    KeyDefinition("DOWN", "▼", usageCode = HidConsts.KEY_DOWN, keyType = KeyType.ARROW, weight = 1.0f),
    KeyDefinition("RIGHT", "►", usageCode = HidConsts.KEY_RIGHT, keyType = KeyType.ARROW, weight = 1.0f)
)
