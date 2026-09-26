package com.example.ui.components

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothHidUiState
import com.example.usb.UsbMouseState
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

@Composable
fun StatusDashboard(
    btState: BluetoothHidUiState,
    usbState: UsbMouseState,
    onScanDevices: () -> Unit,
    onConnect: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenFullscreenKeyboard: () -> Unit,
    onShowLimitations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = btState.connectionState == BluetoothProfile.STATE_CONNECTED
    val isConnecting = btState.connectionState == BluetoothProfile.STATE_CONNECTING
    val targetName = btState.connectedDevice?.name ?: btState.targetDevice?.name ?: "None"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("status_dashboard")
    ) {
        // App Header: Title & Limitation Info Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BT Keyboard & Mouse",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Bluetooth HID Device & OTG Mouse Bridge",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onShowLimitations,
                    modifier = Modifier.testTag("limitations_info_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "API Limitations Info",
                        tint = CyanNeon
                    )
                }

                // Global Mode Badge: 🟢 HID ACTIVE vs IDLE
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (btState.isHidActive) GreenActive.copy(alpha = 0.2f)
                            else DarkSurfaceElevated
                        )
                        .border(
                            1.dp,
                            if (btState.isHidActive) GreenActive else DarkBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("hid_mode_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (btState.isHidActive) GreenActive else TextMuted)
                        )
                        Text(
                            text = if (btState.isHidActive) "🟢 HID ACTIVE" else "⚪ IDLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (btState.isHidActive) GreenActive else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Top Status Summary Grid: Bluetooth, Target, HID
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusCard(
                title = "Bluetooth",
                value = if (btState.isBtEnabled) "ON" else "OFF",
                valueColor = if (btState.isBtEnabled) GreenActive else RedAlert,
                icon = Icons.Default.Bluetooth,
                modifier = Modifier.weight(1f)
            )

            StatusCard(
                title = "Target",
                value = targetName,
                valueColor = if (btState.targetDevice != null) CyanNeon else TextSecondary,
                icon = Icons.Default.TabletAndroid,
                modifier = Modifier.weight(1.3f)
            )

            StatusCard(
                title = "HID Status",
                value = when (btState.connectionState) {
                    BluetoothProfile.STATE_CONNECTED -> "Connected"
                    BluetoothProfile.STATE_CONNECTING -> "Connecting"
                    BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
                    else -> "Disconnected"
                },
                valueColor = when (btState.connectionState) {
                    BluetoothProfile.STATE_CONNECTED -> GreenActive
                    BluetoothProfile.STATE_CONNECTING -> AmberWarning
                    else -> TextSecondary
                },
                icon = Icons.Default.BluetoothConnected,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main 4 Control Buttons: [ SCAN DEVICES ] [ CONNECT ] [ START ] [ STOP ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // SCAN DEVICES
            OutlinedButton(
                onClick = onScanDevices,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("scan_devices_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("SCAN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // CONNECT / DISCONNECT
            OutlinedButton(
                onClick = onConnect,
                enabled = btState.targetDevice != null && !isConnecting,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isConnected) AmberWarning else BlueElectric
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isConnected) AmberWarning else BlueElectric
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("connect_button")
            ) {
                Text(
                    text = if (isConnected) "DISCONNECT" else "CONNECT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // START (ENGAGE HID FORWARDING) - Strictly enabled ONLY when a target BT device is connected!
            Button(
                onClick = onStart,
                enabled = isConnected && !btState.isHidActive,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenActive,
                    contentColor = DarkBg,
                    disabledContainerColor = DarkSurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("start_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("START", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }

            // STOP (HALT HID FORWARDING)
            Button(
                onClick = onStop,
                enabled = btState.isHidActive,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedAlert,
                    contentColor = Color.White,
                    disabledContainerColor = DarkSurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // When START is pressed / HID ACTIVE, show prominent Open Fullscreen Keyboard button!
        AnimatedVisibility(visible = btState.isHidActive) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenFullscreenKeyboard,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = DarkBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("open_fullscreen_keyboard_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OPEN FULL SCREEN KEYBOARD (LANDSCAPE)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Helper hint if Start is disabled because BT is not connected yet
        if (!isConnected && !btState.isHidActive) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (btState.targetDevice == null)
                    "⚠️ Please scan and connect a target Bluetooth device (Phone B/Tablet) first to enable START."
                else
                    "⚠️ Device '${targetName}' is not connected yet. Tap CONNECT above first.",
                fontSize = 11.sp,
                color = AmberWarning,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Diagnostic Tri-Fold Status Panel: KEYBOARD | MOUSE | TARGET DEVICE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBg)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // KEYBOARD
            DetailStatusRow(
                icon = Icons.Default.Keyboard,
                section = "KEYBOARD",
                label1 = "Virtual Keyboard:",
                val1 = "READY",
                val1Color = GreenActive,
                label2 = "HID App:",
                val2 = if (btState.isAppRegistered) "Registered" else "Pending",
                val2Color = if (btState.isAppRegistered) CyanNeon else AmberWarning
            )

            // MOUSE
            DetailStatusRow(
                icon = Icons.Default.Mouse,
                section = "MOUSE",
                label1 = "USB Mouse:",
                val1 = if (usbState.isUsbMouseConnected) "Connected (${usbState.mouseDeviceName})" else "Disconnected",
                val1Color = if (usbState.isUsbMouseConnected) GreenActive else TextSecondary,
                label2 = "OTG Port:",
                val2 = if (usbState.isOtgConnected) "Connected" else "Disconnected",
                val2Color = if (usbState.isOtgConnected) CyanNeon else TextSecondary
            )

            // TARGET DEVICE
            DetailStatusRow(
                icon = Icons.Default.TabletAndroid,
                section = "TARGET DEVICE",
                label1 = "Phone/Tablet:",
                val1 = targetName,
                val1Color = if (btState.targetDevice != null) CyanNeon else TextSecondary,
                label2 = "HID:",
                val2 = if (isConnected) "Connected" else "Disconnected",
                val2Color = if (isConnected) GreenActive else TextMuted
            )
        }

        // Live status message banner
        if (btState.statusMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = btState.statusMessage,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (btState.isHidActive) GreenActive else CyanNeon,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Non-fatal error notice if ROM lacks HID Device Profile
        if (!btState.isHidSupportedOnDevice && btState.errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(RedAlert.copy(alpha = 0.15f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RedAlert,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = btState.errorMessage,
                    fontSize = 10.sp,
                    color = RedAlert
                )
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    value: String,
    valueColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBg)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DetailStatusRow(
    icon: ImageVector,
    section: String,
    label1: String,
    val1: String,
    val1Color: Color,
    label2: String,
    val2: String,
    val2Color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = section,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label1,
                fontSize = 10.sp,
                color = TextMuted
            )
            Text(
                text = val1,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = val1Color,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label2,
                fontSize = 10.sp,
                color = TextMuted
            )
            Text(
                text = val2,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = val2Color
            )
        }
    }
}
