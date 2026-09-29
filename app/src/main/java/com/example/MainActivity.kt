package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.PointerIcon
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bluetooth.BluetoothHidManager
import com.example.bluetooth.HidConsts
import com.example.keyboard.AndroidKeyToHid
import com.example.ui.components.DeviceScanDialog
import com.example.ui.components.FullscreenLandscapeKeyboard
import com.example.ui.components.GamingModeOverlay
import com.example.ui.components.LimitationsInfoDialog
import com.example.ui.components.MouseCaptureOverlay
import com.example.ui.components.StatusDashboard
import com.example.ui.components.VirtualKeyboard
import com.example.ui.theme.BtKeyboardTheme
import com.example.ui.theme.DarkBg
import com.example.usb.UsbMouseDetector
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var hidManager: BluetoothHidManager
    private lateinit var usbDetector: UsbMouseDetector
    private var lastMouseX = Float.NaN
    private var lastMouseY = Float.NaN
    private var mouseAccumulatorX = 0f
    private var mouseAccumulatorY = 0f
    private var lastSentButtons: Byte = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hidManager = BluetoothHidManager(applicationContext)
        usbDetector = UsbMouseDetector(applicationContext)

        // On Android Oreo+, set captured pointer listener directly on decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.setOnCapturedPointerListener { _, event ->
                handleCapturedPointer(event)
            }
        }

        setContent {
            BtKeyboardTheme {
                MainScreen(
                    hidManager = hidManager,
                    usbDetector = usbDetector,
                    onRequestPointerCapture = {
                        requestMousePointerCapture()
                    },
                    onReleasePointerCapture = {
                        releaseMousePointerCapture()
                    }
                )
            }
        }
    }

    private fun requestMousePointerCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.isFocusable = true
            window.decorView.isFocusableInTouchMode = true
            window.decorView.requestFocus()
            window.decorView.requestPointerCapture()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.decorView.pointerIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL)
        }
        usbDetector.setPointerCaptureActive(true)
    }

    private fun releaseMousePointerCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.releasePointerCapture()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.decorView.pointerIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_DEFAULT)
        }
        usbDetector.setPointerCaptureActive(false)
        lastMouseX = Float.NaN
        lastMouseY = Float.NaN
        mouseAccumulatorX = 0f
        mouseAccumulatorY = 0f
        lastSentButtons = 0
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && hidManager.uiState.value.isHidActive) {
            requestMousePointerCapture()
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
        usbDetector.setPointerCaptureActive(hasCapture)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.decorView.pointerIcon = if (hidManager.uiState.value.isHidActive) {
                PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL)
            } else {
                PointerIcon.getSystemIcon(this, PointerIcon.TYPE_DEFAULT)
            }
        }
        if (!hasCapture && hidManager.uiState.value.isHidActive) {
            window.decorView.post {
                if (hidManager.uiState.value.isHidActive) {
                    requestMousePointerCapture()
                }
            }
        }
    }

    private fun isMouseEvent(event: MotionEvent): Boolean {
        val src = event.source
        return (src and InputDevice.SOURCE_MOUSE != 0) ||
               (src and InputDevice.SOURCE_CLASS_POINTER != 0 && event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) ||
               (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) ||
               event.isFromSource(InputDevice.SOURCE_MOUSE)
    }

    // Unified mouse processing with sub-pixel accumulator and tablet screen scaling
    private fun processMouseMotionEvent(event: MotionEvent): Boolean {
        if (!hidManager.uiState.value.isHidActive) return false

        // Keep pointer capture continuously locked & local cursor invisible on Phone A
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !window.decorView.hasPointerCapture()) {
            requestMousePointerCapture()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.decorView.pointerIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL)
        }

        // 1. Extract raw relative motion (unbounded) or continuous fallback deltas
        val rawRelX = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
        } else 0f
        val rawRelY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
        } else 0f

        val dx: Float
        val dy: Float

        if (rawRelX != 0f || rawRelY != 0f) {
            dx = rawRelX
            dy = rawRelY
            lastMouseX = Float.NaN
            lastMouseY = Float.NaN
        } else {
            if (!lastMouseX.isNaN() && !lastMouseY.isNaN()) {
                dx = event.x - lastMouseX
                dy = event.y - lastMouseY
            } else {
                dx = 0f
                dy = 0f
            }
            lastMouseX = event.x
            lastMouseY = event.y
        }

        // 2. High-precision tablet scaling with sub-pixel accumulator
        // 2.2x scaling allows cursor to effortlessly cover the full tablet screen without getting stuck at phone edges
        val tabletScale = 2.2f
        mouseAccumulatorX += dx * tabletScale
        mouseAccumulatorY += dy * tabletScale

        val sendDx = mouseAccumulatorX.toInt().coerceIn(-127, 127).toByte()
        val sendDy = mouseAccumulatorY.toInt().coerceIn(-127, 127).toByte()

        mouseAccumulatorX -= sendDx.toFloat()
        mouseAccumulatorY -= sendDy.toFloat()

        // 3. Extract mouse buttons (Left, Right, Middle)
        val buttons = event.buttonState
        var buttonMask: Byte = 0
        if ((buttons and MotionEvent.BUTTON_PRIMARY) != 0 ||
            (event.action == MotionEvent.ACTION_DOWN && buttons == 0)
        ) {
            buttonMask = (buttonMask.toInt() or HidConsts.MOUSE_BTN_LEFT.toInt()).toByte()
        }
        if ((buttons and MotionEvent.BUTTON_SECONDARY) != 0) {
            buttonMask = (buttonMask.toInt() or HidConsts.MOUSE_BTN_RIGHT.toInt()).toByte()
        }
        if ((buttons and MotionEvent.BUTTON_TERTIARY) != 0) {
            buttonMask = (buttonMask.toInt() or HidConsts.MOUSE_BTN_MIDDLE.toInt()).toByte()
        }
        if (event.action == MotionEvent.ACTION_UP) {
            buttonMask = 0
        }

        // 4. Extract scroll wheel
        val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
        val wheelByte = (vScroll * 1.5f).toInt().coerceIn(-127, 127).toByte()

        // 5. Only dispatch when there is actual movement, wheel or button change
        if (sendDx != 0.toByte() || sendDy != 0.toByte() || wheelByte != 0.toByte() || buttonMask != lastSentButtons) {
            lastSentButtons = buttonMask
            hidManager.sendMouseMotion(buttonMask, sendDx, sendDy, wheelByte)
        }

        return true // Completely consumed! Phone A never clicks its own screen!
    }

    // Android Oreo+ official pointer capture callback
    private fun handleCapturedPointer(event: MotionEvent): Boolean {
        return processMouseMotionEvent(event)
    }

    // Intercept physical USB mouse relative motion and clicks
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (hidManager.uiState.value.isHidActive &&
            (isMouseEvent(event) || (event.source and InputDevice.SOURCE_CLASS_POINTER != 0))
        ) {
            return processMouseMotionEvent(event)
        }
        return super.dispatchGenericMotionEvent(event)
    }

    // Intercept USB mouse touch events to prevent Phone A from clicking its own UI when active
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (hidManager.uiState.value.isHidActive && isMouseEvent(event)) {
            return processMouseMotionEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (hidManager.uiState.value.isHidActive) {
            // Filter out mouse-generated Android BACK key events so right-click is strictly sent as HID mouse button 2
            if (event.source and InputDevice.SOURCE_MOUSE != 0 && event.keyCode == KeyEvent.KEYCODE_BACK) {
                return true
            }

            val hidCode = AndroidKeyToHid.mapKeyCodeToHid(event.keyCode)
            val modifier = AndroidKeyToHid.mapModifierMask(event.keyCode)

            if (modifier != 0.toByte()) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    hidManager.setModifier(modifier, true)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    hidManager.setModifier(modifier, false)
                }
                return true
            }

            if (hidCode != HidConsts.KEY_NONE) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    hidManager.sendKeyDown(hidCode)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    hidManager.sendKeyUp(hidCode)
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        usbDetector.checkConnectedDevices()
    }

    override fun onDestroy() {
        super.onDestroy()
        hidManager.cleanUp()
        usbDetector.cleanUp()
    }
}

@Composable
fun MainScreen(
    hidManager: BluetoothHidManager,
    usbDetector: UsbMouseDetector,
    onRequestPointerCapture: () -> Unit,
    onReleasePointerCapture: () -> Unit
) {
    val btState by hidManager.uiState.collectAsStateWithLifecycle()
    val usbState by usbDetector.state.collectAsStateWithLifecycle()

    var showScanDialog by remember { mutableStateOf(false) }
    var showLimitationsDialog by remember { mutableStateOf(false) }
    var showFullscreenKeyboard by remember { mutableStateOf(false) }
    var showGamePadOverlay by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Permissions check
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            hidManager.initProfileProxy()
            hidManager.refreshPairedDevices()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Bluetooth permissions are required for HID Device connection.")
            }
        }
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(requiredPermissions)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. TOP STATUS DASHBOARD
            item {
                StatusDashboard(
                    btState = btState,
                    usbState = usbState,
                    onScanDevices = { showScanDialog = true },
                    onConnect = {
                        if (btState.targetDevice != null) {
                            if (btState.connectedDevice != null) {
                                hidManager.disconnectTargetDevice()
                            } else {
                                hidManager.connectTargetDevice()
                            }
                        } else {
                            showScanDialog = true
                        }
                    },
                    onStart = {
                        val isConnected = btState.connectionState == android.bluetooth.BluetoothProfile.STATE_CONNECTED
                        if (isConnected) {
                            hidManager.startForwarding()
                            onRequestPointerCapture()
                        } else {
                            // Do NOT allow start without a real connection
                            scope.launch {
                                val devName = btState.targetDevice?.name ?: "target device"
                                snackbarHostState.showSnackbar("Cannot start: Please connect to '$devName' first via the CONNECT button.")
                            }
                        }
                    },
                    onStop = {
                        hidManager.stopForwarding()
                        onReleasePointerCapture()
                    },
                    onOpenFullscreenKeyboard = {
                        showFullscreenKeyboard = true
                    },
                    onOpenGamePad = {
                        showGamePadOverlay = true
                    },
                    onShowLimitations = { showLimitationsDialog = true }
                )
            }

            // 2. MOUSE FORWARDING & TRACKPAD OVERLAY
            item {
                MouseCaptureOverlay(
                    usbState = usbState,
                    isHidActive = btState.isHidActive,
                    hidManager = hidManager,
                    onRequestPointerCapture = onRequestPointerCapture
                )
            }

            // 3. FULL PC / LAPTOP VIRTUAL KEYBOARD
            item {
                VirtualKeyboard(
                    hidManager = hidManager,
                    isHidActive = btState.isHidActive,
                    onOpenFullscreen = {
                        showFullscreenKeyboard = true
                    }
                )
            }
        }

        // Modals
        if (showScanDialog) {
            DeviceScanDialog(
                pairedDevices = btState.pairedDevices,
                discoveredDevices = btState.discoveredDevices,
                selectedDevice = btState.targetDevice,
                connectedDevice = btState.connectedDevice,
                isScanning = btState.isScanning,
                onStartScan = { hidManager.startDiscovery() },
                onStopScan = { hidManager.stopDiscovery() },
                onSelectDevice = { dev ->
                    hidManager.selectTargetDevice(dev)
                },
                onConnectDevice = { dev ->
                    hidManager.selectTargetDevice(dev)
                    hidManager.connectTargetDevice(dev)
                    showScanDialog = false
                },
                onDismiss = { showScanDialog = false }
            )
        }

        if (showLimitationsDialog) {
            LimitationsInfoDialog(
                onDismiss = { showLimitationsDialog = false }
            )
        }

        // Fullscreen Landscape Virtual Keyboard view
        if (showFullscreenKeyboard) {
            FullscreenLandscapeKeyboard(
                hidManager = hidManager,
                isHidActive = btState.isHidActive,
                onClose = { showFullscreenKeyboard = false }
            )
        }

        // Dedicated Game Pad Mode Overlay (Runs separately on request)
        if (showGamePadOverlay) {
            GamingModeOverlay(
                hidManager = hidManager,
                usbState = usbState,
                isHidActive = btState.isHidActive,
                onRequestPointerCapture = onRequestPointerCapture,
                onClose = { showGamePadOverlay = false }
            )
        }
    }
}
