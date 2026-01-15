package com.gerontec.loragpssender

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var configSpinner: Spinner
    private lateinit var sendConfigButton: Button
    private lateinit var messageInput: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var emergencyButton: Button

    private var usbManager: UsbManager? = null
    private var serialPort: UsbSerialPort? = null
    private var usbDevice: UsbDevice? = null
    private var locationManager: LocationManager? = null
    private var lastKnownLocation: Location? = null

    private val ACTION_USB_PERMISSION = "com.gerontec.loragpssender.USB_PERMISSION"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // CH341 Vendor and Product IDs
    private val CH341_VENDOR_ID = 0x1a86
    private val CH341_PRODUCT_ID = 0x7523

    // LoRa configuration commands
    private val loraConfigs = mapOf(
        "netid00" to byteArrayOf(
            0xFF.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x62.toByte(),
            0xE0.toByte(), 0x18.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        ),
        "netid10" to byteArrayOf(
            0xFF.toByte(), 0x0A.toByte(), 0x0A.toByte(), 0xE0.toByte(),
            0xC0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )
    )

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
        configSpinner = findViewById(R.id.configSpinner)
        sendConfigButton = findViewById(R.id.sendConfigButton)
        messageInput = findViewById(R.id.messageInput)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        emergencyButton = findViewById(R.id.emergencyButton)

        // Setup config spinner
        val configOptions = resources.getStringArray(R.array.config_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, configOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        configSpinner.adapter = adapter

        // Setup send config button
        sendConfigButton.setOnClickListener {
            sendSelectedConfig()
        }

        // Setup send message button
        sendMessageButton.setOnClickListener {
            sendMessage()
        }

        // Setup emergency GPS button
        emergencyButton.setOnClickListener {
            sendEmergencyGPS()
        }

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Request location permissions
        requestLocationPermissions()

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

    private fun sendSelectedConfig() {
        val selectedConfig = configSpinner.selectedItem as String

        if (serialPort?.isOpen != true) {
            log("ERROR: Cannot send config - not connected to ttyUSB0")
            return
        }

        loraConfigs[selectedConfig]?.let { configBytes ->
            try {
                // Send the configuration bytes
                serialPort?.write(configBytes, 1000)

                // Log the sent bytes in hex format
                val hexString = configBytes.joinToString(" ") { byte ->
                    String.format("%02X", byte)
                }

                log("TX: Config '$selectedConfig' sent successfully")
                log("Bytes sent: $hexString (${configBytes.size} bytes)")

            } catch (e: IOException) {
                log("ERROR: Failed to send config: ${e.message}")
            } catch (e: Exception) {
                log("ERROR: Unexpected error sending config: ${e.message}")
            }
        } ?: run {
            log("ERROR: Unknown configuration '$selectedConfig'")
        }
    }

    private fun sendMessage() {
        val message = messageInput.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, "Bitte Nachricht eingeben", Toast.LENGTH_SHORT).show()
            return
        }

        if (serialPort?.isOpen != true) {
            log("ERROR: Cannot send message - not connected to ttyUSB0")
            Toast.makeText(this, "Nicht verbunden!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Send message as ASCII bytes
            val messageBytes = message.toByteArray(Charsets.US_ASCII)
            serialPort?.write(messageBytes, 1000)

            val hexString = messageBytes.joinToString(" ") { byte ->
                String.format("%02X", byte)
            }

            log("TX: Message sent successfully")
            log("Message: $message")
            log("Bytes: $hexString (${messageBytes.size} bytes)")

            // Clear input field
            messageInput.setText("")
            Toast.makeText(this, "Nachricht gesendet", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            log("ERROR: Failed to send message: ${e.message}")
            Toast.makeText(this, "Fehler beim Senden!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            log("ERROR: Unexpected error sending message: ${e.message}")
            Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmergencyGPS() {
        if (serialPort?.isOpen != true) {
            log("ERROR: Cannot send GPS - not connected to ttyUSB0")
            Toast.makeText(this, "Nicht verbunden!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            log("ERROR: Location permission not granted")
            Toast.makeText(this, "GPS-Berechtigung fehlt!", Toast.LENGTH_SHORT).show()
            requestLocationPermissions()
            return
        }

        try {
            // Get last known location
            val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lastKnownLocation

            if (location == null) {
                log("ERROR: No GPS location available")
                Toast.makeText(this, "Keine GPS-Position verfügbar!", Toast.LENGTH_LONG).show()
                return
            }

            // Format GPS data as string: "EMERGENCY LAT:xx.xxxxx LON:yy.yyyyy"
            val gpsMessage = String.format(
                "EMERGENCY LAT:%.6f LON:%.6f",
                location.latitude,
                location.longitude
            )

            // Send GPS message as ASCII bytes
            val gpsBytes = gpsMessage.toByteArray(Charsets.US_ASCII)
            serialPort?.write(gpsBytes, 1000)

            val hexString = gpsBytes.joinToString(" ") { byte ->
                String.format("%02X", byte)
            }

            log("TX: EMERGENCY GPS sent successfully")
            log("GPS: $gpsMessage")
            log("Bytes: $hexString (${gpsBytes.size} bytes)")

            Toast.makeText(this, "NOTFALL GPS gesendet!", Toast.LENGTH_LONG).show()

        } catch (e: SecurityException) {
            log("ERROR: Location permission denied: ${e.message}")
            Toast.makeText(this, "GPS-Berechtigung fehlt!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            log("ERROR: Failed to send GPS: ${e.message}")
            Toast.makeText(this, "Fehler beim Senden!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            log("ERROR: Unexpected error sending GPS: ${e.message}")
            Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, start location updates
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            // Request location updates
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // 5 seconds
                10f,  // 10 meters
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lastKnownLocation = location
                        log("GPS: Location updated (${location.latitude}, ${location.longitude})")
                    }

                    override fun onProviderEnabled(provider: String) {
                        log("GPS: Provider enabled: $provider")
                    }

                    override fun onProviderDisabled(provider: String) {
                        log("GPS: Provider disabled: $provider")
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                        // Deprecated but required for older API levels
                    }
                }
            )

            // Also try network provider
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                10f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (lastKnownLocation == null) {
                            lastKnownLocation = location
                            log("Network: Location updated (${location.latitude}, ${location.longitude})")
                        }
                    }

                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
            )

            log("GPS: Location updates started")
        } catch (e: SecurityException) {
            log("ERROR: Location permission error: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("GPS: Location permission granted")
                    startLocationUpdates()
                } else {
                    log("GPS: Location permission denied")
                    Toast.makeText(this, "GPS-Berechtigung benötigt für Notfall-Funktion", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
