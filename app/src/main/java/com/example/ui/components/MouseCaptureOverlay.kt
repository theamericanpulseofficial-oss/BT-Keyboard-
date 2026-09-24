package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.usb.UsbMouseState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkKeyBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MouseCaptureOverlay(
    usbState: UsbMouseState,
    isHidActive: Boolean,
    hidManager: BluetoothHidManager,
    onRequestPointerCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var lastDx by remember { mutableIntStateOf(0) }
    var lastDy by remember { mutableIntStateOf(0) }
    var activeButtonsMask by remember { mutableStateOf(0.toByte()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("mouse_capture_card")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = null,
                    tint = if (usbState.isUsbMouseConnected) CyanNeon else TextSecondary
                )
                Column {
                    Text(
                        text = "MOUSE FORWARDING & TRACKPAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (usbState.hasPointerCapture) "Pointer Capture Active (Mouse locked to forward)" else "Pointer Capture Inactive",
                        fontSize = 10.sp,
                        color = if (usbState.hasPointerCapture) GreenActive else TextMuted
                    )
                }
            }

            // Quick capture / focus button
            if (isHidActive && !usbState.hasPointerCapture) {
                Surface(
                    onClick = onRequestPointerCapture,
                    shape = RoundedCornerShape(6.dp),
                    color = CyanNeon.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon),
                    modifier = Modifier.testTag("request_capture_button")
                ) {
                    Text(
                        text = "LOCK MOUSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-function Touch / Mouse Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBg)
                .border(
                    1.dp,
                    if (usbState.hasPointerCapture) GreenActive.copy(alpha = 0.5f) else DarkBorder,
                    RoundedCornerShape(8.dp)
                )
                .pointerInput(isHidActive) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isHidActive) {
                                val dx = (dragAmount.x * 1.5f).toInt().coerceIn(-127, 127).toByte()
                                val dy = (dragAmount.y * 1.5f).toInt().coerceIn(-127, 127).toByte()
                                lastDx = dx.toInt()
                                lastDy = dy.toInt()
                                hidManager.sendMouseMotion(activeButtonsMask, dx, dy, 0)
                            }
                        }
                    )
                }
                .pointerInput(isHidActive) {
                    detectTapGestures(
                        onTap = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            if (isHidActive) {
                                hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_LEFT, 0, 0, 0)
                                view.postDelayed({
                                    hidManager.sendMouseMotion(0, 0, 0, 0)
                                }, 30)
                            }
                        }
                    )
                }
                .testTag("trackpad_touch_area"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (usbState.hasPointerCapture) {
                    Icon(
                        imageVector = Icons.Default.Usb,
                        contentDescription = null,
                        tint = GreenActive,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "REAL USB MOUSE CAPTURED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenActive
                    )
                    Text(
                        text = "Hardware moves are forwarded directly via Bluetooth HID",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "VIRTUAL TRACKPAD & OTG MOUSE ZONE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = if (usbState.isUsbMouseConnected)
                            "USB Mouse detected: ${usbState.mouseDeviceName}. Press START to lock & forward."
                        else
                            "Connect USB mouse via OTG adapter, or drag here as trackpad",
                        fontSize = 10.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (isHidActive) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ΔX: $lastDx  |  ΔY: $lastDy  |  Buttons: 0x${activeButtonsMask.toInt().toString(16)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanNeon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Physical Mouse Buttons (Left, Middle, Right) + Scroll
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left Button
            MouseButton(
                label = "LEFT CLICK",
                modifier = Modifier.weight(1.5f),
                onClick = {
                    if (isHidActive) {
                        hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_LEFT, 0, 0, 0)
                        view.postDelayed({ hidManager.sendMouseMotion(0, 0, 0, 0) }, 30)
                    }
                },
                testTag = "mouse_left_btn"
            )

            // Middle Button
            MouseButton(
                label = "MID",
                modifier = Modifier.weight(0.9f),
                onClick = {
                    if (isHidActive) {
                        hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_MIDDLE, 0, 0, 0)
                        view.postDelayed({ hidManager.sendMouseMotion(0, 0, 0, 0) }, 30)
                    }
                },
                testTag = "mouse_mid_btn"
            )

            // Right Button
            MouseButton(
                label = "RIGHT CLICK",
                modifier = Modifier.weight(1.5f),
                onClick = {
                    if (isHidActive) {
                        hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_RIGHT, 0, 0, 0)
                        view.postDelayed({ hidManager.sendMouseMotion(0, 0, 0, 0) }, 30)
                    }
                },
                testTag = "mouse_right_btn"
            )

            // Scroll Up
            MouseButton(
                label = "▲",
                modifier = Modifier.weight(0.7f),
                onClick = {
                    if (isHidActive) {
                        hidManager.sendMouseMotion(0, 0, 0, 1.toByte())
                    }
                },
                testTag = "mouse_scroll_up_btn"
            )

            // Scroll Down
            MouseButton(
                label = "▼",
                modifier = Modifier.weight(0.7f),
                onClick = {
                    if (isHidActive) {
                        hidManager.sendMouseMotion(0, 0, 0, (-1).toByte())
                    }
                },
                testTag = "mouse_scroll_down_btn"
            )
        }
    }
}

@Composable
fun MouseButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) CyanNeon.copy(alpha = 0.3f) else DarkKeyBg)
            .border(1.dp, if (isPressed) CyanNeon else DarkBorder, RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
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
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) CyanNeon else TextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
