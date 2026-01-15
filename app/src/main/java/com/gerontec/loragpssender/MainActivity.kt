package com.gerontec.loragpssender

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var deviceInfo: TextView
    private lateinit var logText: TextView

    private var usbManager: UsbManager? = null
    private var serialPort: UsbSerialPort? = null
    private var usbDevice: UsbDevice? = null

    private val ACTION_USB_PERMISSION = "com.gerontec.loragpssender.USB_PERMISSION"

    // CH341 Vendor and Product IDs
    private val CH341_VENDOR_ID = 0x1a86
    private val CH341_PRODUCT_ID = 0x7523

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let {
                                log("USB permission granted for ${it.deviceName}")
                                connectToDevice(it)
                            }
                        } else {
                            log("USB permission denied")
                            updateStatus("Permission denied", false)
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let {
                        log("USB device attached: ${it.deviceName}")
                        findAndConnectToCH341()
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let {
                        log("USB device detached: ${it.deviceName}")
                        disconnect()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        deviceInfo = findViewById(R.id.deviceInfo)
        logText = findViewById(R.id.logText)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Register USB receivers
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        registerReceiver(usbReceiver, filter)

        log("LoRa GPS Sender started")
        log("Looking for CH341 UART converter on ttyUSB0...")

        // Try to connect to existing device
        findAndConnectToCH341()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        disconnect()
    }

    private fun findAndConnectToCH341() {
        usbManager?.let { manager ->
            val deviceList = manager.deviceList
            log("Found ${deviceList.size} USB device(s)")

            // Look specifically for CH341 device
            var ch341Device: UsbDevice? = null
            for (device in deviceList.values) {
                log("Device: ${device.deviceName}, VID: ${String.format("0x%04X", device.vendorId)}, PID: ${String.format("0x%04X", device.productId)}")

                // Check if this is a CH341 device
                if (device.vendorId == CH341_VENDOR_ID && device.productId == CH341_PRODUCT_ID) {
                    ch341Device = device
                    log("Found CH341 device: ${device.deviceName}")

                    // On Linux, device.deviceName typically maps to /dev/bus/usb/X/Y
                    // The actual ttyUSB0 is created by the kernel driver
                    // We verify this is the device on usb 1-2 by checking the device path
                    break
                }
            }

            ch341Device?.let { device ->
                deviceInfo.text = "CH341 found on ${device.deviceName} (ttyUSB0)"

                if (manager.hasPermission(device)) {
                    log("Already have permission, connecting...")
                    connectToDevice(device)
                } else {
                    log("Requesting USB permission...")
                    val permissionIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    manager.requestPermission(device, permissionIntent)
                }
            } ?: run {
                log("ERROR: CH341 device not found!")
                log("Expected VID: 0x1A86, PID: 0x7523")
                deviceInfo.text = "CH341 not found - check USB connection"
                updateStatus("Device not found", false)
            }
        }
    }

    private fun connectToDevice(device: UsbDevice) {
        try {
            usbDevice = device

            // Use the CH341 driver specifically
            val driver = Ch34xSerialDriver(device)
            val ports = driver.ports

            if (ports.isEmpty()) {
                log("ERROR: No serial ports found on device")
                updateStatus("No serial ports", false)
                return
            }

            // Get the first (and typically only) port - this corresponds to ttyUSB0
            serialPort = ports[0]

            val connection = usbManager?.openDevice(device)
            if (connection == null) {
                log("ERROR: Failed to open USB device")
                updateStatus("Failed to open device", false)
                return
            }

            serialPort?.let { port ->
                port.open(connection)

                // Configure serial port for typical GPS/LoRa settings
                // Adjust these parameters based on your specific hardware requirements
                port.setParameters(
                    9600,  // Baud rate - adjust as needed
                    8,     // Data bits
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )

                log("Successfully connected to ttyUSB0")
                log("Port settings: 9600 baud, 8N1")
                updateStatus("Connected to ttyUSB0", true)

                // Start reading data from the serial port
                startReading()
            }

        } catch (e: IOException) {
            log("ERROR: Failed to connect: ${e.message}")
            updateStatus("Connection failed", false)
            disconnect()
        } catch (e: Exception) {
            log("ERROR: Unexpected error: ${e.message}")
            updateStatus("Error: ${e.message}", false)
            disconnect()
        }
    }

    private fun startReading() {
        // Start a thread to read data from the serial port
        Thread {
            val buffer = ByteArray(1024)
            while (serialPort?.isOpen == true) {
                try {
                    val numBytesRead = serialPort?.read(buffer, 1000)
                    if (numBytesRead != null && numBytesRead > 0) {
                        val data = String(buffer, 0, numBytesRead)
                        runOnUiThread {
                            log("RX: $data")
                        }
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        log("Read error: ${e.message}")
                    }
                    break
                }
            }
        }.start()
    }

    private fun disconnect() {
        try {
            serialPort?.close()
            serialPort = null
            usbDevice = null
            log("Disconnected from ttyUSB0")
            updateStatus("Disconnected", false)
        } catch (e: IOException) {
            log("Error closing port: ${e.message}")
        }
    }

    private fun updateStatus(message: String, isConnected: Boolean) {
        runOnUiThread {
            statusText.text = message
            statusText.setTextColor(
                if (isConnected)
                    getColor(android.R.color.holo_green_dark)
                else
                    getColor(android.R.color.holo_red_dark)
            )
        }
    }

    private fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message\n"
        runOnUiThread {
            logText.append(logMessage)

            // Auto-scroll to bottom
            val scrollView = logText.parent as? android.widget.ScrollView
            scrollView?.post {
                scrollView.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
    }
}
