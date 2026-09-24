package com.example.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.input.InputManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.view.InputDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsbMouseState(
    val isOtgConnected: Boolean = false,
    val isUsbMouseConnected: Boolean = false,
    val mouseDeviceName: String = "None",
    val hasPointerCapture: Boolean = false,
    val connectedDevicesSummary: String = "No USB OTG device detected",
    val statusMessage: String = "Waiting for OTG mouse connection"
)

class UsbMouseDetector(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager

    private val _state = MutableStateFlow(UsbMouseState())
    val state: StateFlow<UsbMouseState> = _state.asStateFlow()

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED,
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    checkConnectedDevices()
                }
            }
        }
    }

    private val inputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            checkConnectedDevices()
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            checkConnectedDevices()
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            checkConnectedDevices()
        }
    }

    init {
        registerReceivers()
        checkConnectedDevices()
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(usbReceiver, filter)
        inputManager?.registerInputDeviceListener(inputDeviceListener, null)
    }

    fun setPointerCaptureActive(active: Boolean) {
        _state.value = _state.value.copy(
            hasPointerCapture = active,
            statusMessage = if (active) "Pointer captured. Mouse moves forwarded." else "Pointer released."
        )
    }

    fun checkConnectedDevices() {
        var hasUsbDevice = false
        var hasMouse = false
        var mouseName = "None"
        val summaries = mutableListOf<String>()

        // 1. Check via UsbManager (Hardware USB OTG level)
        val deviceList: Map<String, UsbDevice>? = usbManager?.deviceList
        if (!deviceList.isNullOrEmpty()) {
            hasUsbDevice = true
            for ((_, device) in deviceList) {
                val isHidMouse = checkDeviceIsMouse(device)
                val devInfo = "${device.productName ?: device.deviceName} (Vendor: ${device.vendorId.toString(16)}, Product: ${device.productId.toString(16)})"
                summaries.add(devInfo)
                if (isHidMouse) {
                    hasMouse = true
                    mouseName = device.productName ?: "USB HID Mouse"
                }
            }
        }

        // 2. Check via InputManager (Android OS input subsystem level)
        val deviceIds = inputManager?.inputDeviceIds ?: intArrayOf()
        for (id in deviceIds) {
            val dev = InputDevice.getDevice(id) ?: continue
            val isPointerMouse = (dev.sources and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE
            if (isPointerMouse && !dev.isVirtual) {
                hasMouse = true
                if (mouseName == "None") {
                    mouseName = dev.name
                }
                if (!hasUsbDevice) {
                    // Even if USB enumeration permission isn't granted, InputManager sees real OTG mouse!
                    hasUsbDevice = true
                    summaries.add("${dev.name} (via InputManager)")
                }
            }
        }

        val summaryText = if (summaries.isEmpty()) {
            "No USB OTG device detected"
        } else {
            summaries.joinToString("; ")
        }

        _state.value = _state.value.copy(
            isOtgConnected = hasUsbDevice,
            isUsbMouseConnected = hasMouse,
            mouseDeviceName = mouseName,
            connectedDevicesSummary = summaryText,
            statusMessage = if (hasMouse) "USB Mouse ready ($mouseName)" else if (hasUsbDevice) "USB device connected (non-mouse)" else "No OTG mouse connected"
        )
    }

    private fun checkDeviceIsMouse(device: UsbDevice): Boolean {
        // USB Class 3 = HID
        if (device.deviceClass == UsbConstants.USB_CLASS_HID) return true
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            if (intf.interfaceClass == UsbConstants.USB_CLASS_HID) {
                // subclass 1 = Boot Interface, protocol 2 = Mouse
                if (intf.interfaceProtocol == 2 || intf.interfaceSubclass == 1) {
                    return true
                }
                return true // Generic HID device connected
            }
        }
        return false
    }

    fun cleanUp() {
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {}
        try {
            inputManager?.unregisterInputDeviceListener(inputDeviceListener)
        } catch (_: Exception) {}
    }
}
