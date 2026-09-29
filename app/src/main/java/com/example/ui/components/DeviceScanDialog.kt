package com.example.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenActive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("MissingPermission")
@Composable
fun DeviceScanDialog(
    pairedDevices: List<BluetoothDevice>,
    discoveredDevices: List<BluetoothDevice>,
    selectedDevice: BluetoothDevice?,
    connectedDevice: BluetoothDevice?,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (BluetoothDevice) -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("device_scan_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
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
                            imageVector = Icons.Default.TabletAndroid,
                            contentDescription = null,
                            tint = CyanNeon
                        )
                        Text(
                            text = "Target Device (Phone B / Tablet)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scan Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isScanning) "Searching nearby devices..." else "Select device to connect:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (isScanning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                                color = CyanNeon
                            )
                            OutlinedButton(
                                onClick = onStopScan,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("STOP", fontSize = 10.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartScan,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SCAN", fontSize = 10.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Device List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (pairedDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "PAIRED DEVICES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(pairedDevices) { device ->
                            DeviceItemCard(
                                device = device,
                                isSelected = selectedDevice?.address == device.address,
                                isConnected = connectedDevice?.address == device.address,
                                isPaired = true,
                                onClick = { onSelectDevice(device) },
                                onConnectClick = { onConnectDevice(device) }
                            )
                        }
                    }

                    if (discoveredDevices.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "DISCOVERED DEVICES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(discoveredDevices) { device ->
                            DeviceItemCard(
                                device = device,
                                isSelected = selectedDevice?.address == device.address,
                                isConnected = connectedDevice?.address == device.address,
                                isPaired = false,
                                onClick = { onSelectDevice(device) },
                                onConnectClick = { onConnectDevice(device) }
                            )
                        }
                    }

                    if (pairedDevices.isEmpty() && discoveredDevices.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No devices found yet.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Ensure Phone B or Tablet has Bluetooth turned ON and is discoverable.",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Done button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dialog_done_button")
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItemCard(
    device: BluetoothDevice,
    isSelected: Boolean,
    isConnected: Boolean,
    isPaired: Boolean,
    onClick: () -> Unit,
    onConnectClick: () -> Unit
) {
    val devName = try {
        device.name ?: "Unknown Device"
    } catch (_: Exception) {
        "Unknown Device"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isConnected) GreenActive.copy(alpha = 0.15f) else if (isSelected) CyanNeon.copy(alpha = 0.12f) else DarkBg)
            .border(
                1.dp,
                if (isConnected) GreenActive else if (isSelected) CyanNeon else DarkBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Bluetooth else Icons.Default.Devices,
                    contentDescription = null,
                    tint = if (isConnected) GreenActive else if (isSelected) CyanNeon else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = devName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isConnected) GreenActive else if (isSelected) CyanNeon else TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${device.address} ${if (isConnected) "• CONNECTED" else if (isPaired) "• Paired" else "• Discovered"}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isConnected) GreenActive else TextMuted
                    )
                }
            }

            // Quick Connect Button or Connected Status
            if (isConnected) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenActive.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenActive)
                ) {
                    Text(
                        text = "CONNECTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenActive,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onConnectClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = DarkBg
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("CONNECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
