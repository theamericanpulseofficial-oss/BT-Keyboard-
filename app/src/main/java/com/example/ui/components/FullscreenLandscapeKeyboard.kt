package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.keyboard.KeyboardLayouts
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    // Force landscape mode while this screen is active, restore sensor on exit
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
    var isFnActive by remember { mutableStateOf(false) }
    var isCapsLockActive by remember { mutableStateOf(false) }
    var isNumLockActive by remember { mutableStateOf(true) }

    var selectedClusterTab by remember { mutableStateOf(0) } // 0: Complete PC (Side-by-side all keys), 1: Main, 2: Nav, 3: Numpad

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("fullscreen_keyboard_screen")
    ) {
        // Top Bar: LEDs, Cluster Tab Switcher, Quick Trackpad toggle, Exit Fullscreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status & LED
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (isHidActive) GreenActive else TextMuted)
                    )
                    Text(
                        text = if (isHidActive) "🟢 HID ACTIVE" else "DISCONNECTED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHidActive) GreenActive else TextSecondary
                    )
                }

                LedIndicator(label = "NUM", active = isNumLockActive)
                LedIndicator(label = "CAPS", active = isCapsLockActive)
                LedIndicator(label = "SCRL", active = false)
            }

            // Center: Cluster Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyboardTabChip("Full PC (All Keys)", selectedClusterTab == 0) { selectedClusterTab = 0 }
                KeyboardTabChip("Main QWERTY", selectedClusterTab == 1) { selectedClusterTab = 1 }
                KeyboardTabChip("Nav / Arrows", selectedClusterTab == 2) { selectedClusterTab = 2 }
                KeyboardTabChip("Numpad", selectedClusterTab == 3) { selectedClusterTab = 3 }
            }

            // Right: Quick Mouse buttons & Exit Fullscreen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick Mouse Click Actions in Landscape top-bar
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

        // Full Screen Keyboard Workspace
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(DarkSurface, RoundedCornerShape(10.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            when (selectedClusterTab) {
                0 -> {
                    // Full PC Layout: Main QWERTY + Nav Cluster + Arrows + Numpad all visible together!
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
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
                                handleFullscreenKey(
                                    key = key,
                                    hidManager = hidManager,
                                    isShiftActive = isShiftActive,
                                    onCapsToggle = { isCapsLockActive = !isCapsLockActive },
                                    onFnToggle = { isFnActive = !isFnActive },
                                    onShiftConsumed = {
                                        if (isShiftActive) {
                                            isShiftActive = false
                                            hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, false)
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.width(720.dp)
                        )

                        // Vertical divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxSize()
                                .background(DarkBorder)
                        )

                        NavAndArrowCluster(
                            onKeyAction = { key ->
                                handleFullscreenKey(
                                    key = key,
                                    hidManager = hidManager,
                                    isShiftActive = isShiftActive,
                                    onCapsToggle = {},
                                    onFnToggle = {},
                                    onShiftConsumed = {}
                                )
                            },
                            modifier = Modifier.width(160.dp)
                        )

                        // Vertical divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxSize()
                                .background(DarkBorder)
                        )

                        NumpadCluster(
                            isNumLockActive = isNumLockActive,
                            onNumLockToggle = {
                                isNumLockActive = !isNumLockActive
                                hidManager.sendKeyPress(HidConsts.KEY_NUMLOCK)
                            },
                            onKeyAction = { key ->
                                handleFullscreenKey(
                                    key = key,
                                    hidManager = hidManager,
                                    isShiftActive = isShiftActive,
                                    onCapsToggle = {},
                                    onFnToggle = {},
                                    onShiftConsumed = {}
                                )
                            },
                            modifier = Modifier.width(190.dp)
                        )
                    }
                }
                1 -> {
                    // Main QWERTY stretched across landscape screen
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
                            handleFullscreenKey(
                                key = key,
                                hidManager = hidManager,
                                isShiftActive = isShiftActive,
                                onCapsToggle = { isCapsLockActive = !isCapsLockActive },
                                onFnToggle = { isFnActive = !isFnActive },
                                onShiftConsumed = {
                                    if (isShiftActive) {
                                        isShiftActive = false
                                        hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, false)
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                2 -> {
                    // Nav and Arrows centered
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        NavAndArrowCluster(
                            onKeyAction = { key ->
                                handleFullscreenKey(
                                    key = key,
                                    hidManager = hidManager,
                                    isShiftActive = isShiftActive,
                                    onCapsToggle = {},
                                    onFnToggle = {},
                                    onShiftConsumed = {}
                                )
                            },
                            modifier = Modifier.width(320.dp)
                        )
                    }
                }
                3 -> {
                    // Numpad centered
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        NumpadCluster(
                            isNumLockActive = isNumLockActive,
                            onNumLockToggle = {
                                isNumLockActive = !isNumLockActive
                                hidManager.sendKeyPress(HidConsts.KEY_NUMLOCK)
                            },
                            onKeyAction = { key ->
                                handleFullscreenKey(
                                    key = key,
                                    hidManager = hidManager,
                                    isShiftActive = isShiftActive,
                                    onCapsToggle = {},
                                    onFnToggle = {},
                                    onShiftConsumed = {}
                                )
                            },
                            modifier = Modifier.width(340.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun handleFullscreenKey(
    key: KeyDefinition,
    hidManager: BluetoothHidManager,
    isShiftActive: Boolean,
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
