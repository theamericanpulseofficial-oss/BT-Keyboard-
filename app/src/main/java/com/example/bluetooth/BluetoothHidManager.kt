package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private const val TAG = "BTHidManager"

data class BluetoothHidUiState(
    val isBtSupported: Boolean = true,
    val isBtEnabled: Boolean = false,
    val isHidSupportedOnDevice: Boolean = false,
    val isProxyBound: Boolean = false,
    val isAppRegistered: Boolean = false,
    val connectionState: Int = BluetoothProfile.STATE_DISCONNECTED,
    val connectedDevice: BluetoothDevice? = null,
    val targetDevice: BluetoothDevice? = null,
    val isHidActive: Boolean = false,
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val discoveredDevices: List<BluetoothDevice> = emptyList(),
    val isScanning: Boolean = false,
    val numLockLed: Boolean = false,
    val capsLockLed: Boolean = false,
    val scrollLockLed: Boolean = false,
    val statusMessage: String = "Initializing Bluetooth HID...",
    val errorMessage: String? = null
)

class BluetoothHidManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _uiState = MutableStateFlow(
        BluetoothHidUiState(
            isBtSupported = bluetoothAdapter != null,
            isBtEnabled = bluetoothAdapter?.isEnabled == true
        )
    )
    val uiState: StateFlow<BluetoothHidUiState> = _uiState.asStateFlow()

    private var hidDevice: BluetoothHidDevice? = null
    val gameKeyMapper = com.example.keyboard.GameKeyMapper()

    // Dedicated high-priority single thread for zero-latency HID dispatch (avoids UI binder stalls)
    private val hidDispatcher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "GamingFastHidDispatcher").apply {
            priority = Thread.MAX_PRIORITY
        }
    }
    private val scope = CoroutineScope(Dispatchers.Main)

    // Current HID Keyboard state (up to 6 keys + modifiers)
    private val pressedKeys = LinkedHashSet<Byte>()
    private var activeModifiers: Byte = 0
    private var lastMouseButtons: Byte = 0

    // Ultra-low latency mouse accumulator & atomic coalescer (prevents BT buffer bloat)
    private val pendingDx = java.util.concurrent.atomic.AtomicInteger(0)
    private val pendingDy = java.util.concurrent.atomic.AtomicInteger(0)
    private val pendingWheel = java.util.concurrent.atomic.AtomicInteger(0)
    private val currentMouseButtons = java.util.concurrent.atomic.AtomicInteger(0)
    private val isMouseFlushScheduled = java.util.concurrent.atomic.AtomicBoolean(false)

    // Packet rate & latency metrics
    private var packetCount = 0
    private var lastRateCalcTime = System.currentTimeMillis()
    private val _packetRateHz = MutableStateFlow(0)
    val packetRateHz: StateFlow<Int> = _packetRateHz.asStateFlow()

    // Reusable buffers to guarantee ZERO garbage collection allocations on low-memory (1GB RAM) devices
    private val keyboardReportBuffer = ByteArray(8)
    private val mouseReportBuffer = ByteArray(4)

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.i(TAG, "BluetoothProfile.HID_DEVICE service connected")
                hidDevice = proxy as BluetoothHidDevice
                _uiState.value = _uiState.value.copy(
                    isHidSupportedOnDevice = true,
                    isProxyBound = true,
                    statusMessage = "HID Device profile ready. Registering SDP..."
                )
                registerHidApp()
                checkConnectedDevicesNow()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.w(TAG, "BluetoothProfile.HID_DEVICE service disconnected")
                hidDevice = null
                _uiState.value = _uiState.value.copy(
                    isProxyBound = false,
                    isAppRegistered = false,
                    connectionState = BluetoothProfile.STATE_DISCONNECTED,
                    statusMessage = "HID Device service disconnected"
                )
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered, device=${pluggedDevice?.name}")
            _uiState.value = _uiState.value.copy(
                isAppRegistered = registered,
                statusMessage = if (registered) "HID Registered. Ready to pair/connect." else "HID Registration failed."
            )
            if (registered && pluggedDevice != null) {
                _uiState.value = _uiState.value.copy(targetDevice = pluggedDevice)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            val stateStr = when (state) {
                BluetoothProfile.STATE_CONNECTED -> "Connected"
                BluetoothProfile.STATE_CONNECTING -> "Connecting..."
                BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting..."
                else -> "Disconnected"
            }
            Log.d(TAG, "onConnectionStateChanged: ${device?.name ?: "Unknown"} -> $stateStr ($state)")
            _uiState.value = _uiState.value.copy(
                connectionState = state,
                connectedDevice = if (state == BluetoothProfile.STATE_CONNECTED) device else null,
                targetDevice = device ?: _uiState.value.targetDevice,
                isHidActive = if (state == BluetoothProfile.STATE_CONNECTED) true else if (state == BluetoothProfile.STATE_DISCONNECTED) false else _uiState.value.isHidActive,
                statusMessage = "Target ${device?.name ?: "Device"}: $stateStr"
            )
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(TAG, "onGetReport: type=$type, id=$id")
            if (device == null) return
            val data = when (id) {
                HidConsts.REPORT_ID_KEYBOARD -> buildKeyboardReport()
                HidConsts.REPORT_ID_MOUSE -> byteArrayOf(lastMouseButtons, 0, 0, 0)
                else -> ByteArray(0)
            }
            try {
                hidDevice?.replyReport(device, type, id, data)
            } catch (e: Exception) {
                Log.e(TAG, "Error replying to report", e)
            }
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            Log.d(TAG, "onSetReport: type=$type, id=$id, bytes=${data?.size}")
            // LED state is sent via Output Report (type 2) on Report ID 1 (or 0)
            if (data != null && data.isNotEmpty()) {
                val ledByte = data[0].toInt()
                val num = (ledByte and 0x01) != 0
                val caps = (ledByte and 0x02) != 0
                val scroll = (ledByte and 0x04) != 0
                _uiState.value = _uiState.value.copy(
                    numLockLed = num,
                    capsLockLed = caps,
                    scrollLockLed = scroll
                )
            }
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            Log.d(TAG, "onSetProtocol: protocol=$protocol")
        }

        override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
            Log.d(TAG, "onInterruptData: reportId=$reportId")
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice?) {
            Log.d(TAG, "onVirtualCableUnplug: ${device?.name}")
            _uiState.value = _uiState.value.copy(
                connectionState = BluetoothProfile.STATE_DISCONNECTED,
                connectedDevice = null,
                statusMessage = "Host unplugged virtual cable"
            )
        }
    }

    private val btReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val enabled = state == BluetoothAdapter.STATE_ON
                    _uiState.value = _uiState.value.copy(isBtEnabled = enabled)
                    if (enabled) {
                        initProfileProxy()
                        refreshPairedDevices()
                    }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null) {
                        val currentList = _uiState.value.discoveredDevices.toMutableList()
                        if (currentList.none { it.address == device.address }) {
                            currentList.add(device)
                            _uiState.value = _uiState.value.copy(discoveredDevices = currentList)
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _uiState.value = _uiState.value.copy(isScanning = true, statusMessage = "Scanning for devices...")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _uiState.value = _uiState.value.copy(isScanning = false, statusMessage = "Scan finished.")
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    refreshPairedDevices()
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null) {
                        Log.i(TAG, "ACL Connected to: ${device.name ?: device.address}")
                        checkConnectedDevicesNow(device)
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && _uiState.value.connectedDevice?.address == device.address) {
                        _uiState.value = _uiState.value.copy(
                            connectionState = BluetoothProfile.STATE_DISCONNECTED,
                            connectedDevice = null,
                            isHidActive = false,
                            statusMessage = "Device ${device.name ?: "Target"} disconnected."
                        )
                    }
                }
            }
        }
    }

    init {
        registerBroadcastReceivers()
        if (bluetoothAdapter?.isEnabled == true) {
            initProfileProxy()
            refreshPairedDevices()
        }
    }

    private fun registerBroadcastReceivers() {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(btReceiver, filter)
    }

    @SuppressLint("MissingPermission")
    fun initProfileProxy() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        try {
            val supported = adapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
            if (!supported) {
                Log.e(TAG, "BluetoothProfile.HID_DEVICE not supported by this device/ROM")
                _uiState.value = _uiState.value.copy(
                    isHidSupportedOnDevice = false,
                    statusMessage = "Notice: Bluetooth HID Device profile is unsupported or disabled by this device's ROM.",
                    errorMessage = "This device's manufacturer Bluetooth stack does not support Bluetooth HID Device profile."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting HID_DEVICE profile proxy", e)
            _uiState.value = _uiState.value.copy(
                isHidSupportedOnDevice = false,
                errorMessage = "Error initializing HID profile: ${e.localizedMessage}"
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun registerHidApp() {
        val dev = hidDevice ?: return
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "BT Keyboard & Mouse",
            "Android Virtual Combo Input Device",
            "Android SDK",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            HidConsts.HID_REPORT_DESCRIPTOR
        )

        try {
            val success = dev.registerApp(sdpSettings, null, null, hidDispatcher, hidCallback)
            Log.d(TAG, "registerApp result: $success")
            _uiState.value = _uiState.value.copy(
                statusMessage = if (success) "Registering HID SDP profile..." else "Failed to register HID app."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during registerApp", e)
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to register HID profile: ${e.message}"
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun unregisterHidApp() {
        try {
            hidDevice?.unregisterApp()
            _uiState.value = _uiState.value.copy(isAppRegistered = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering HID app", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun checkConnectedDevicesNow(hintDevice: BluetoothDevice? = null) {
        try {
            val dev = hidDevice
            val connectedList = dev?.connectedDevices
            val activeDevice = hintDevice ?: connectedList?.firstOrNull()

            if (activeDevice != null) {
                _uiState.value = _uiState.value.copy(
                    connectionState = BluetoothProfile.STATE_CONNECTED,
                    connectedDevice = activeDevice,
                    targetDevice = activeDevice,
                    statusMessage = "Target '${activeDevice.name ?: activeDevice.address}': Connected"
                )
            } else if (_uiState.value.targetDevice == null && _uiState.value.pairedDevices.isNotEmpty()) {
                val defaultTarget = _uiState.value.pairedDevices.first()
                _uiState.value = _uiState.value.copy(
                    targetDevice = defaultTarget,
                    statusMessage = "Target: ${defaultTarget.name ?: defaultTarget.address} (Tap CONNECT)"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "checkConnectedDevicesNow error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        try {
            val bonded = adapter.bondedDevices?.toList() ?: emptyList()
            val currentTarget = _uiState.value.targetDevice
            val newTarget = if (currentTarget != null && bonded.any { it.address == currentTarget.address }) {
                currentTarget
            } else {
                bonded.firstOrNull()
            }
            _uiState.value = _uiState.value.copy(
                pairedDevices = bonded,
                targetDevice = newTarget ?: currentTarget,
                statusMessage = if (_uiState.value.connectionState == BluetoothProfile.STATE_CONNECTED)
                    _uiState.value.statusMessage
                else if (newTarget != null)
                    "Selected: ${newTarget.name ?: newTarget.address}. Tap CONNECT to link."
                else
                    _uiState.value.statusMessage
            )
            // If already connected, verify immediately
            checkConnectedDevicesNow()
        } catch (e: Exception) {
            Log.w(TAG, "Cannot get bonded devices: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            _uiState.value = _uiState.value.copy(discoveredDevices = emptyList())
            adapter.startDiscovery()
        } catch (e: Exception) {
            Log.e(TAG, "Cannot start discovery", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        try {
            bluetoothAdapter?.cancelDiscovery()
            _uiState.value = _uiState.value.copy(isScanning = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping discovery", e)
        }
    }

    fun selectTargetDevice(device: BluetoothDevice) {
        _uiState.value = _uiState.value.copy(targetDevice = device)
    }

    @SuppressLint("MissingPermission")
    fun connectTargetDevice(device: BluetoothDevice? = null) {
        val devToConnect = device ?: _uiState.value.targetDevice ?: return
        _uiState.value = _uiState.value.copy(targetDevice = devToConnect, statusMessage = "Connecting to ${devToConnect.name}...")
        try {
            val dev = hidDevice
            if (dev == null) {
                _uiState.value = _uiState.value.copy(statusMessage = "HID service not connected yet. Waiting...")
                initProfileProxy()
                return
            }
            val res = dev.connect(devToConnect)
            Log.d(TAG, "connect() returned: $res")
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to device", e)
            _uiState.value = _uiState.value.copy(errorMessage = "Connection error: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectTargetDevice() {
        val target = _uiState.value.connectedDevice ?: _uiState.value.targetDevice
        if (target != null && hidDevice != null) {
            try {
                hidDevice?.disconnect(target)
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting", e)
            }
        }
        _uiState.value = _uiState.value.copy(
            isHidActive = false,
            connectionState = BluetoothProfile.STATE_DISCONNECTED,
            connectedDevice = null,
            statusMessage = "Disconnected."
        )
    }

    fun startForwarding() {
        _uiState.value = _uiState.value.copy(
            isHidActive = true,
            statusMessage = "🟢 HID ACTIVE - Input forwarding engaged"
        )
    }

    fun stopForwarding() {
        // Send key release reports and zero mouse report
        releaseAllKeys()
        sendMouseMotion(0, 0, 0, 0)
        _uiState.value = _uiState.value.copy(
            isHidActive = false,
            statusMessage = "HID Forwarding stopped. System idle."
        )
    }

    // --- KEYBOARD REPORTS (Ultra-Low Latency & Remapped for Gaming) ---

    fun setModifier(mask: Byte, active: Boolean) {
        hidDispatcher.execute {
            synchronized(this@BluetoothHidManager) {
                activeModifiers = if (active) {
                    (activeModifiers.toInt() or mask.toInt()).toByte()
                } else {
                    (activeModifiers.toInt() and mask.toInt().inv()).toByte()
                }
                sendKeyboardReportDirect()
            }
        }
    }

    fun toggleModifier(mask: Byte): Boolean {
        val isNowActive = (activeModifiers.toInt() and mask.toInt()) == 0
        setModifier(mask, isNowActive)
        return isNowActive
    }

    fun isModifierActive(mask: Byte): Boolean {
        return (activeModifiers.toInt() and mask.toInt()) != 0
    }

    fun sendKeyDown(rawUsageCode: Byte) {
        val usageCode = gameKeyMapper.getMappedKey(rawUsageCode)
        if (usageCode == HidConsts.KEY_NONE) return
        hidDispatcher.execute {
            synchronized(this@BluetoothHidManager) {
                pressedKeys.add(usageCode)
                sendKeyboardReportDirect()
            }
        }
    }

    fun sendKeyUp(rawUsageCode: Byte) {
        val usageCode = gameKeyMapper.getMappedKey(rawUsageCode)
        if (usageCode == HidConsts.KEY_NONE) return
        hidDispatcher.execute {
            synchronized(this@BluetoothHidManager) {
                pressedKeys.remove(usageCode)
                sendKeyboardReportDirect()
            }
        }
    }

    fun sendKeyPress(rawUsageCode: Byte) {
        val usageCode = gameKeyMapper.getMappedKey(rawUsageCode)
        if (usageCode == HidConsts.KEY_NONE) return
        hidDispatcher.execute {
            synchronized(this@BluetoothHidManager) {
                pressedKeys.add(usageCode)
                sendKeyboardReportDirect()
            }
        }
        // Release after tiny 16ms delay (1 frame)
        scope.launch {
            kotlinx.coroutines.delay(16)
            hidDispatcher.execute {
                synchronized(this@BluetoothHidManager) {
                    pressedKeys.remove(usageCode)
                    sendKeyboardReportDirect()
                }
            }
        }
    }

    fun releaseAllKeys() {
        hidDispatcher.execute {
            synchronized(this@BluetoothHidManager) {
                pressedKeys.clear()
                activeModifiers = 0
                sendKeyboardReportDirect()
            }
        }
    }

    private fun buildKeyboardReport(): ByteArray {
        synchronized(this) {
            keyboardReportBuffer[0] = activeModifiers
            keyboardReportBuffer[1] = 0 // reserved
            var idx = 2
            for (k in pressedKeys) {
                if (idx >= 8) break
                keyboardReportBuffer[idx++] = k
            }
            while (idx < 8) {
                keyboardReportBuffer[idx++] = 0
            }
            return keyboardReportBuffer
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendKeyboardReportDirect() {
        val dev = hidDevice ?: return
        val target = _uiState.value.connectedDevice ?: _uiState.value.targetDevice ?: return
        if (!_uiState.value.isHidActive) return

        val report = buildKeyboardReport()
        try {
            dev.sendReport(target, HidConsts.REPORT_ID_KEYBOARD.toInt(), report)
            recordPacketSent()
        } catch (e: Exception) {
            Log.e(TAG, "sendKeyboardReport failed", e)
        }
    }

    // --- MOUSE REPORTS (Ultra-Low Latency FIFO Dispatch - Direct HID Report Delivery) ---

    @SuppressLint("MissingPermission")
    fun sendMouseMotion(buttons: Byte, dx: Byte, dy: Byte, wheel: Byte) {
        if (!_uiState.value.isHidActive) return
        if (dx == 0.toByte() && dy == 0.toByte() && wheel == 0.toByte() && buttons == lastMouseButtons) {
            return
        }
        lastMouseButtons = buttons

        hidDispatcher.execute {
            val dev = hidDevice ?: return@execute
            val target = _uiState.value.connectedDevice ?: _uiState.value.targetDevice ?: return@execute
            if (!_uiState.value.isHidActive) return@execute

            synchronized(mouseReportBuffer) {
                mouseReportBuffer[0] = buttons
                mouseReportBuffer[1] = dx
                mouseReportBuffer[2] = dy
                mouseReportBuffer[3] = wheel
                try {
                    dev.sendReport(target, HidConsts.REPORT_ID_MOUSE.toInt(), mouseReportBuffer)
                    recordPacketSent()
                } catch (e: Exception) {
                    Log.e(TAG, "sendMouseMotion failed", e)
                }
            }
        }
    }

    private fun recordPacketSent() {
        packetCount++
        val now = System.currentTimeMillis()
        if (now - lastRateCalcTime >= 1000) {
            _packetRateHz.value = packetCount
            packetCount = 0
            lastRateCalcTime = now
        }
    }

    fun cleanUp() {
        try {
            context.unregisterReceiver(btReceiver)
        } catch (_: Exception) {}
        try {
            hidDevice?.let { bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, it) }
        } catch (_: Exception) {}
        hidDispatcher.shutdown()
    }
}
