package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothHidManager
import com.example.bluetooth.HidConsts
import com.example.keyboard.KeyDefinition
import com.example.keyboard.KeyType
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.TextPrimary

/**
 * Fullscreen Landscape Laptop Keyboard.
 * Automatically locks orientation to LANDSCAPE and gives a spacious typing experience.
 */
@Composable
fun FullscreenLandscapeKeyboard(
    hidManager: BluetoothHidManager,
    isHidActive: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current

    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    BackHandler {
        onClose()
    }

    var isShiftActive by remember { mutableStateOf(false) }
    var isCtrlActive by remember { mutableStateOf(false) }
    var isAltActive by remember { mutableStateOf(false) }
    var isWinActive by remember { mutableStateOf(false) }
    var isCapsLockActive by remember { mutableStateOf(false) }
    var isFnActive by remember { mutableStateOf(false) }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Landscape Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💻 LAPTOP KEYBOARD (LANDSCAPE)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                LedDot(label = "CAPS", active = isCapsLockActive)
                LedDot(label = "SHIFT", active = isShiftActive)
                LedDot(label = "CTRL", active = isCtrlActive)
                LedDot(label = "HID", active = isHidActive, activeColor = GreenActive)
            }

            // Right Actions: Quick Mouse Clicks & Exit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        if (isHidActive) {
                            hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_LEFT, 0, 0, 0)
                            view.postDelayed({ hidManager.sendMouseMotion(0, 0, 0, 0) }, 30)
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("L-CLICK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        if (isHidActive) {
                            hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_RIGHT, 0, 0, 0)
                            view.postDelayed({ hidManager.sendMouseMotion(0, 0, 0, 0) }, 30)
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("R-CLICK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    onClick = onClose,
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "EXIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Full Screen Laptop Keyboard Body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(DarkSurface, RoundedCornerShape(10.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
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
                    heightDp = 38
                )

                // Row 1: Numbers
                LaptopKeyRow(
                    keys = LAPTOP_NUMBER_ROW,
                    isShiftActive = isShiftActive,
                    isCapsLockActive = isCapsLockActive,
                    isCtrlActive = isCtrlActive,
                    isAltActive = isAltActive,
                    isWinActive = isWinActive,
                    isFnActive = isFnActive,
                    onKeyAction = keyAction,
                    heightDp = 44
                )

                // Row 2: QWERTY
                LaptopKeyRow(
                    keys = LAPTOP_QWERTY_ROW,
                    isShiftActive = isShiftActive,
                    isCapsLockActive = isCapsLockActive,
                    isCtrlActive = isCtrlActive,
                    isAltActive = isAltActive,
                    isWinActive = isWinActive,
                    isFnActive = isFnActive,
                    onKeyAction = keyAction,
                    heightDp = 44
                )

                // Row 3: Home row
                LaptopKeyRow(
                    keys = LAPTOP_HOME_ROW,
                    isShiftActive = isShiftActive,
                    isCapsLockActive = isCapsLockActive,
                    isCtrlActive = isCtrlActive,
                    isAltActive = isAltActive,
                    isWinActive = isWinActive,
                    isFnActive = isFnActive,
                    onKeyAction = keyAction,
                    heightDp = 44
                )

                // Row 4: Bottom row + Up Arrow
                LaptopKeyRow(
                    keys = LAPTOP_BOTTOM_ROW,
                    isShiftActive = isShiftActive,
                    isCapsLockActive = isCapsLockActive,
                    isCtrlActive = isCtrlActive,
                    isAltActive = isAltActive,
                    isWinActive = isWinActive,
                    isFnActive = isFnActive,
                    onKeyAction = keyAction,
                    heightDp = 44
                )

                // Row 5: Space row + Left, Down, Right Arrows
                LaptopKeyRow(
                    keys = LAPTOP_SPACE_ROW,
                    isShiftActive = isShiftActive,
                    isCapsLockActive = isCapsLockActive,
                    isCtrlActive = isCtrlActive,
                    isAltActive = isAltActive,
                    isWinActive = isWinActive,
                    isFnActive = isFnActive,
                    onKeyAction = keyAction,
                    heightDp = 44
                )
            }
        }
    }
}
