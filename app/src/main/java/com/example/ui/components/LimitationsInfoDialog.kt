package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.AmberWarning
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
fun LimitationsInfoDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("limitations_info_dialog")
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanNeon
                        )
                        Text(
                            text = "Android APIs & Device Limitations",
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        FactItem(
                            number = "1",
                            question = "Can Phone A act as a Bluetooth HID Device?",
                            status = "YES (Supported in Android 9+)",
                            statusColor = GreenActive,
                            explanation = "Android 9.0 (API 28) added public BluetoothHidDevice APIs. An app can register an SDP record to advertise and operate as an actual Bluetooth peripheral."
                        )
                    }

                    item {
                        FactItem(
                            number = "2 & 3",
                            question = "Can a normal app register a Bluetooth HID Keyboard & Mouse?",
                            status = "YES (Composite SDP)",
                            statusColor = GreenActive,
                            explanation = "Using BluetoothHidDevice.registerApp() with a composite HID report descriptor, Phone B / tablets detect Phone A as a genuine Bluetooth keyboard and mouse without custom apps needed on Phone B."
                        )
                    }

                    item {
                        FactItem(
                            number = "4 & 5",
                            question = "Can a USB OTG mouse be captured & prevented from controlling Phone A?",
                            status = "YES while App is in Foreground",
                            statusColor = AmberWarning,
                            explanation = "Via Android 8.0+ View.requestPointerCapture(), when pointer capture is active on Phone A, the system mouse pointer is hidden and all relative mouse movements (dX, dY, scroll, buttons) are exclusively routed to this app, preventing clicks on Phone A's system UI. However, Android does NOT allow a normal app to capture mouse input globally when backgrounded."
                        )
                    }

                    item {
                        FactItem(
                            number = "6 & 7",
                            question = "Are Root or AccessibilityService required?",
                            status = "NO (Neither is required)",
                            statusColor = GreenActive,
                            explanation = "BluetoothHidDevice and View.requestPointerCapture() are fully public Android SDK APIs. AccessibilityService cannot synthesize Bluetooth HID packets and is neither required nor suitable."
                        )
                    }

                    item {
                        FactItem(
                            number = "8",
                            question = "Does functionality vary by Android version / OEM manufacturer?",
                            status = "YES (Varies by OEM ROM)",
                            statusColor = RedAlert,
                            explanation = "Some smartphone manufacturers (e.g. certain Huawei, Xiaomi, or older customized vendor builds) compile their Bluetooth stack without the HID Device profile enabled. On standard AOSP, Pixel, and modern Samsung devices, Bluetooth HID Device profile operates normally."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text("UNDERSTOOD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FactItem(
    number: String,
    question: String,
    status: String,
    statusColor: Color,
    explanation: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBg)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$number. $question",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Verdict: $status",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = explanation,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
