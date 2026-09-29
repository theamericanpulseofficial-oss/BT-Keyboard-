package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.PointerIcon
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.bluetooth.BluetoothHidManager
import com.example.bluetooth.HidConsts
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.usb.UsbMouseState

/**
 * Ultra-Low Latency Game Pad Mode Overlay.
 * Runs independently when opened from the top bar.
 * Supports simultaneous physical OTG mouse (Left Click = Fire, Right Click = Scope, Motion = Aim)
 * and ergonomic on-screen controls (WASD, Jump, Sprint, Reload, Weapons).
 */
@Composable
fun GamingModeOverlay(
    hidManager: BluetoothHidManager,
    usbState: UsbMouseState,
    isHidActive: Boolean,
    onRequestPointerCapture: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val packetRate by hidManager.packetRateHz.collectAsState()

    // Immersive landscape orientation for Game Pad
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isHidActive) {
                window.decorView.pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_NULL)
            }
        }
        onRequestPointerCapture()

        onDispose {
            activity?.requestedOrientation = originalOrientation
            val win = activity?.window
            if (win != null) {
                val controller = WindowCompat.getInsetsController(win, win.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler {
        onClose()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Top Header: Status, Mouse Connection State, and Exit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberWarning.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "GAME PAD MODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberWarning
                        )
                    }
                }

                // Mouse Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (usbState.isUsbMouseConnected || usbState.hasPointerCapture) GreenActive.copy(alpha = 0.15f) else DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (usbState.isUsbMouseConnected || usbState.hasPointerCapture) GreenActive else DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mouse,
                            contentDescription = null,
                            tint = if (usbState.isUsbMouseConnected || usbState.hasPointerCapture) GreenActive else CyanNeon,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (usbState.isUsbMouseConnected || usbState.hasPointerCapture)
                                "MOUSE CONNECTED: LEFT=FIRE | RIGHT=SCOPE"
                            else
                                "MOUSE: READY / TOUCH TRACKPAD ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (usbState.isUsbMouseConnected || usbState.hasPointerCapture) GreenActive else CyanNeon
                        )
                    }
                }

                // Polling Rate Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = GreenActive, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${packetRate}Hz",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GreenActive
                        )
                    }
                }
            }

            // Close / Exit Game Pad
            Surface(
                onClick = onClose,
                shape = RoundedCornerShape(8.dp),
                color = RedAlert.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedAlert)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Game Pad", tint = RedAlert, modifier = Modifier.size(14.dp))
                    Text("EXIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RedAlert)
                }
            }
        }

        // Main Game Pad Controls Layout:
        // [LEFT: WASD Movement] | [CENTER: Aim & Fire Pad (works in tandem with physical mouse)] | [RIGHT: Action Buttons]
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT: WASD Ergonomic Gaming D-Pad (Touch & Hold to walk/run)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🕹️ MOVEMENT (WASD)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )

                    // WASD Cluster
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Forward (W)
                        GamePadButton(
                            label = "W",
                            subLabel = "FORWARD",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_W) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_W) },
                            modifier = Modifier.size(width = 66.dp, height = 48.dp)
                        )

                        // Middle: A (Left), S (Back), D (Right)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GamePadButton(
                                label = "A",
                                subLabel = "LEFT",
                                onDown = { hidManager.sendKeyDown(HidConsts.KEY_A) },
                                onUp = { hidManager.sendKeyUp(HidConsts.KEY_A) },
                                modifier = Modifier.size(width = 60.dp, height = 48.dp)
                            )
                            GamePadButton(
                                label = "S",
                                subLabel = "BACK",
                                onDown = { hidManager.sendKeyDown(HidConsts.KEY_S) },
                                onUp = { hidManager.sendKeyUp(HidConsts.KEY_S) },
                                modifier = Modifier.size(width = 66.dp, height = 48.dp)
                            )
                            GamePadButton(
                                label = "D",
                                subLabel = "RIGHT",
                                onDown = { hidManager.sendKeyDown(HidConsts.KEY_D) },
                                onUp = { hidManager.sendKeyUp(HidConsts.KEY_D) },
                                modifier = Modifier.size(width = 60.dp, height = 48.dp)
                            )
                        }
                    }

                    // Sprint & Crouch modifiers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GamePadButton(
                            label = "SHIFT",
                            subLabel = "SPRINT",
                            onDown = { hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, true) },
                            onUp = { hidManager.setModifier(HidConsts.MOD_LEFT_SHIFT, false) },
                            activeColor = AmberWarning,
                            modifier = Modifier.size(width = 78.dp, height = 36.dp)
                        )
                        GamePadButton(
                            label = "CTRL",
                            subLabel = "CROUCH",
                            onDown = { hidManager.setModifier(HidConsts.MOD_LEFT_CTRL, true) },
                            onUp = { hidManager.setModifier(HidConsts.MOD_LEFT_CTRL, false) },
                            activeColor = BlueElectric,
                            modifier = Modifier.size(width = 78.dp, height = 36.dp)
                        )
                    }
                }
            }

            // CENTER: Mouse Aim / Touch Aim Trackpad & On-Screen Click Buttons
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 CAMERA AIM & FIRE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (usbState.isUsbMouseConnected) "USB Mouse active" else "Touch to aim",
                            fontSize = 10.sp,
                            color = if (usbState.isUsbMouseConnected) GreenActive else TextMuted
                        )
                    }

                    // Touch Drag Surface (also acts as aim pad if physical mouse is not connected)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (isHidActive) {
                                            val dx = (dragAmount.x * 1.5f).toInt().coerceIn(-127, 127).toByte()
                                            val dy = (dragAmount.y * 1.5f).toInt().coerceIn(-127, 127).toByte()
                                            hidManager.sendMouseMotion(0, dx, dy, 0)
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (usbState.isUsbMouseConnected) "🖱️ USB MOUSE READY" else "👆 AIM TRACKPAD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (usbState.isUsbMouseConnected) GreenActive else TextSecondary
                            )
                            Text(
                                text = if (usbState.isUsbMouseConnected)
                                    "Physical mouse movements & clicks routed directly"
                                else
                                    "Swipe here to turn camera in game",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // FIRE (Mouse Left Click) & AIM (Mouse Right Click)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // FIRE (Left Click)
                        GamePadButton(
                            label = "🔥 SHOOT / FIRE",
                            subLabel = "LEFT CLICK",
                            onDown = {
                                if (isHidActive) hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_LEFT, 0, 0, 0)
                            },
                            onUp = {
                                if (isHidActive) hidManager.sendMouseMotion(0, 0, 0, 0)
                            },
                            activeColor = RedAlert,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )

                        // ADS (Right Click)
                        GamePadButton(
                            label = "🎯 AIM / SCOPE",
                            subLabel = "RIGHT CLICK",
                            onDown = {
                                if (isHidActive) hidManager.sendMouseMotion(HidConsts.MOUSE_BTN_RIGHT, 0, 0, 0)
                            },
                            onUp = {
                                if (isHidActive) hidManager.sendMouseMotion(0, 0, 0, 0)
                            },
                            activeColor = BlueElectric,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )
                    }
                }
            }

            // RIGHT: Action Buttons (Jump, Weapons, Reload, Interact)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ ACTIONS & COMBAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )

                    // Weapons 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GamePadButton(
                            label = "1",
                            subLabel = "PRIMARY",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_1) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_1) },
                            modifier = Modifier.size(width = 58.dp, height = 38.dp)
                        )
                        GamePadButton(
                            label = "2",
                            subLabel = "SECONDARY",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_2) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_2) },
                            modifier = Modifier.size(width = 58.dp, height = 38.dp)
                        )
                        GamePadButton(
                            label = "3",
                            subLabel = "MELEE",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_3) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_3) },
                            modifier = Modifier.size(width = 58.dp, height = 38.dp)
                        )
                    }

                    // Tactical R (Reload), F (Use), G (Grenade)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GamePadButton(
                            label = "R",
                            subLabel = "RELOAD",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_R) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_R) },
                            modifier = Modifier.size(width = 60.dp, height = 42.dp)
                        )
                        GamePadButton(
                            label = "F",
                            subLabel = "USE / LOOT",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_F) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_F) },
                            modifier = Modifier.size(width = 60.dp, height = 42.dp)
                        )
                        GamePadButton(
                            label = "G",
                            subLabel = "GRENADE",
                            onDown = { hidManager.sendKeyDown(HidConsts.KEY_G) },
                            onUp = { hidManager.sendKeyUp(HidConsts.KEY_G) },
                            modifier = Modifier.size(width = 60.dp, height = 42.dp)
                        )
                    }

                    // Big JUMP (Spacebar) Button
                    GamePadButton(
                        label = "SPACEBAR",
                        subLabel = "JUMP",
                        onDown = { hidManager.sendKeyDown(HidConsts.KEY_SPACE) },
                        onUp = { hidManager.sendKeyUp(HidConsts.KEY_SPACE) },
                        activeColor = GreenActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    )
                }
            }
        }
    }
}

/**
 * Ergonomic Game Pad Button supporting Instant Press-Down and Release-Up callbacks.
 */
@Composable
fun GamePadButton(
    label: String,
    subLabel: String? = null,
    onDown: () -> Unit,
    onUp: () -> Unit,
    activeColor: Color = CyanNeon,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) activeColor.copy(alpha = 0.35f) else DarkSurfaceElevated)
            .border(
                1.5.dp,
                if (isPressed) activeColor else DarkBorder,
                RoundedCornerShape(8.dp)
            )
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onDown()
                        tryAwaitRelease()
                        isPressed = false
                        onUp()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = if (label.length > 5) 11.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isPressed) activeColor else TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed) activeColor else TextMuted
                )
            }
        }
    }
}
