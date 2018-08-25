/* ***********
* Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
* Programmer: Joao Lopes
* Module    : BLE - Utilities to BLE
* Comments  : Based on codes of Nordic (https://github.com/NordicSemiconductor)
* Versions  :
* -------  --------    -------------------------
* 0.1.0    20/08/18    First version
**/

/*
 * Notes:
 *
 */

package com.example.util.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import com.example.util.*

import java.io.UnsupportedEncodingException

import com.example.util.extentions.extAlert
import com.example.util.extentions.extExpandStr
import com.example.util.extentions.extShowException
import com.example.util.extentions.extShowToast

/////// Class BLE

class BLE constructor (activity: Activity,
                       deviceName: String,
                       strBTNotAvaliable: String,
                       strDeviceHasDisconnected: String,
                       strNotPossibleConnDevice: String,
                       strBLEUartServiceNotActive: String,
                       strDeviceNotHaveBLE: String,
                       strNotPossibleStartUARTBLEService: String,
                       strBLEDeviceNull: String,
                       strDeviceNotSupportUART: String,
                       strTurnOnGPS: String,
                       strGPSNotEnabled: String,
                       bleHandler: BLEHandler) {

    //////// Companion (static)

    companion object {

        // Public

        const val REQUEST_ENABLE_BT = 1001                          // To enable Bluetooth

        var debugExtra: Boolean = false                             // Show extra debug in Android logs ?

        // Private

        private const val BLE_TIME_SCAN: Long = 5000                // Search device for some time
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 1002 // Permission to scanner
        private const val MAX_ATTEMPT_CONNECTION = 3                // Max attempt

    }

    ///// Variables public

    private var timeoutConnection: Long = 20000                         // Timeout of connection (can be changed)

    var device: BluetoothDevice? = null                         // Device BLE
        private set

    var nrAttemptConnection: Int = 0                            // Current Attempt
    var nrErrorsConnection: Int = 0                             // Connection error number (does not change with each attempt)

    var available: Boolean = false                              // BLE available ?
        private set
    var scanning: Boolean = false                               // Scanning device BLE
        private set
    var connecting: Boolean = false                             // Connecting ?
        private set
    var deviceConnected: Boolean = false                        // Device connected ?
        get () { return bleUartService != null && field }
        private set

    var sending = false                                         // Sendind BLE message now ?
        private set

    var connectionFinished = false                              // Is the connection terminated? ?
        private set

    var lastAddrConnected: String? = null                       // Last device connected
        private set

    var bleGreaterRSSI: Int = 0                                 // Greater RSSI value
        private set

    ///// Privates

    private var deviceName: String                              // Inicio do name do device, o endereco deste serￃﾡ colocado no final
    private lateinit var adapter: BluetoothAdapter              // Adaptador BT

    private var bufferLine = StringBuffer()                     // Buffer for receive line

    private var timeLastRecvData: Long = 0                      // Time of receive data

    private var bleUtilHandler: BLEHandler? = null            // BT handler

    private var turnOffBluetooth: Boolean = false               // Turn off the Bluetooth ?

    private var handlerTimeoutConnection: Handler? = null       // Handler to connection timeout

    // Bluetooh Low Energy

    private var bleUartService: BLEUartService? = null          // Service UART BLE of Nordic
    private var bleDeviceCloser: BluetoothDevice? = null        // Closer device founded
    private var bleHandlerScan: Handler? = null                 // Handler to scan

    // Activity

    private var activity: Activity                             // Activity

    // Strings for internationalization - to not have dependency of project - to easy to update Utils

    private var strBTNotAvaliable: String? = null
    private var strDeviceHasDisconnected: String? = null
    private var strNotPossibleConnDevice: String? = null
    private var strDeviceNotHaveBLE: String? = null
    private var strNotPossibleStartUARTBLEService: String? = null
    private var strBLEUartServiceNotActive: String? = null
    private var strBLEDeviceNull: String? = null
    private var strDeviceNotSupportUART: String? = null
    private var strTurnOnGPS: String? = null
    private var strGPSNotEnabled: String? = null

    ////////////////// Initializer Block

    init {

        // Initialize

        this.activity = activity
        this.deviceName = deviceName
        this.bleUtilHandler = bleHandler

        // Strings

        this.strBTNotAvaliable = strBTNotAvaliable
        this.strBLEUartServiceNotActive = strBLEUartServiceNotActive
        this.strDeviceNotHaveBLE = strDeviceNotHaveBLE
        this.strNotPossibleStartUARTBLEService = strNotPossibleStartUARTBLEService
        this.strDeviceHasDisconnected = strDeviceHasDisconnected
        this.strNotPossibleConnDevice = strNotPossibleConnDevice
        this.strBLEDeviceNull = strBLEDeviceNull
        this.strDeviceNotSupportUART = strDeviceNotSupportUART
        this.strTurnOnGPS = strTurnOnGPS
        this.strGPSNotEnabled = strGPSNotEnabled

        // Initialize BLE

        initialize()
    }

    ////////////////// Routines Bluetooth (BLE)

    // Initialize

    private fun initialize() {

        // BT Adapter

        this.adapter = BluetoothAdapter.getDefaultAdapter()
//        if (this.adapter == null) {
//            this.activity.extShowException(this.strBTNotAvaliable)
//            this.activity.finish()
//            return
//        }

        //Turn off on out? (if not activated before)

        this.turnOffBluetooth = !adapter.isEnabled

        // Verify permittions

        verifyPermissions()

        // Initialize the Bluetooth

        initializeBluetooth()

        // Initialize the BLE

        bleInitialize()

        // End previous connection, if any

        finalizeConnection()

        // Initialize variables

        this.device = null
        this.scanning = false
        this.connecting = false
        this.sending = false
        this.deviceConnected = Util.isEmulator // If you are an emulator consider connected
        this.handlerTimeoutConnection = null

        // Last connected device

        this.lastAddrConnected = Preferences.read("BT_ADDR", null)

    }

    private fun verifyPermissions() {

        // For Android 6 or higher you have to check the location permission
        // Seen at https://stackoverflow.com/questions/45197191/how-can-i-modify-bluetoothlegatt-to-enable-ble-device-scanning-on-android-6-0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            PERMISSION_REQUEST_COARSE_LOCATION)
                }
            }
        }

    }

    private fun initializeBluetooth() {

        // ****** BLUETOOTH

        try {

            // Execute on the Main Thread

            val handler = Handler(Looper.getMainLooper())
            handler.post {

                // BT Classic

                // UUID for serial communication (SPP)
                // serialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

                // Get notifications

                try {
                    activity.unregisterReceiver(broadcastReceiver)
                } catch (e: Exception) {
                }

                if (debugExtra) {
                    logD("before intent")
                }
                val filter = IntentFilter()
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                activity.registerReceiver(broadcastReceiver, filter)

                // Is Bluetooth enabled?

                available = adapter.isEnabled
            }


        } catch (e: Exception) {
            this.activity.extShowException(e)
        }

    }

    // Finalize BT

    fun finalize() {

        // End BLE service

        // Execute on the Main Thread

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            try {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(bleUartStatusChangeReceiver)
            } catch (ignore: Exception) {
                logD("finalizeConnection: error -> $ignore")
            }

            try {
                activity.unbindService(bleUartServiceConnection)
            } catch (ignore: Exception) {
                logD("finalizeConnection: error -> $ignore")
            }

            if (bleUartService != null) {
                try {
                    bleUartService!!.stopSelf()
                } catch (ignore: Exception) {
                    logD("finalizeConnection: error -> $ignore")
                }

                bleUartService = null
            }

            try {
                activity.unregisterReceiver(broadcastReceiver)
            } catch (e: Exception) {
            }
        }
    }

    // Treat request return

    fun treatOnActivityResultOk(requestCode: Int) {

        // Bluetooth on

        if (requestCode == REQUEST_ENABLE_BT) {

            // Authorized access to Bluetooth

            available = true

            // Initialize

            initializeVariables()

            // Callback for connection

            if (bleUtilHandler != null) {
                bleUtilHandler!!.onConnect()
            }

            // BLE -> service is started?

            if (bleUartService != null) {

                // Connect to the last device

                connectLastDevice()

            }
        }
    }

    // Treats bluetooth status

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            // Treat BT notifications

            try {

                val action = intent.action

                if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {

                    // state has changed

                    val estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                    if (estado == BluetoothAdapter.STATE_ON) {

                        // Turn on Bluetooth

                        available = true

                        // Callback for connection

                        if (bleUtilHandler != null) {
                            bleUtilHandler!!.onConnect()
                        }

                        // Connect to the last device

                        if (lastAddrConnected != null) {
                            connectLastDevice()
                        }

                    } else if (estado == BluetoothAdapter.STATE_OFF) {

                        // Turn off Bluetooth

                        available = false

                        // Callback to abort connection

                        if (bleUtilHandler != null) {
                            bleUtilHandler!!.onAbortConnection(strBTNotAvaliable)
                        }

                    }

                }


            } catch (e: Exception) {
                //this.activity.extShowException(e)
                logE("onReceive")
                e.printStackTrace()
            }

        }
    }

    // Search device connected via Bluetooth

    fun scanDevice() {

        try {

            logD("scanDevice")

            nrAttemptConnection = 1
            connectionFinished = false

            // Bluetooth Low Energy

            scanDevice(true)

        } catch (e: Exception) {
            this.activity.extShowException(e)
        }

    }

    private fun initializeVariables() {

        // Initialize control variables

        sending = false
        bufferLine = StringBuffer()
    }

    fun sendMessage(message: String) {

        val MAX_SIZE = if (bleUartService!!.mtuChanged > 0) bleUartService!!.mtuChanged else 20

        // Sending ?

        if (sending) {

            // If sending, send this with delay

            val handler = Handler()
            handler.postDelayed({

                logI("Sending now, this send will after delay!")
                sendMessage(message)
            }, 500)

            return
        }

        sending = true

        // Send a message via BLE

        if (bleUartService == null) {

            if (Util.isEmulator == false) {
                this.activity.extShowException(strBLEUartServiceNotActive)
            }
            return
        }

        logD("[${message.length}]: ${message.extExpandStr()}")

        // In separate Thread

        try {

            object : Thread() {
                override fun run() {

                    // Version 2 -> String

                    var posBegin = 0
                    val size = message.length
                    var posEnd = if (size > MAX_SIZE) MAX_SIZE else size

                    // Process, sending parts if they are greater than the maximum

                    do {
                        //  System.out.println("pos inic " + posInic + " fim " + posFim);
                        val part = message.substring(posBegin, posEnd)

                        if (part.length > 0) {

                            if (debugExtra) {
                                logD("sending part [${part.length}]: ${part.extExpandStr()}")
                            }
                            var dados = ByteArray(0)
                            try {
                                dados = part.toByteArray(charset("UTF-8"))
                            } catch (e: UnsupportedEncodingException) {
                                e.printStackTrace()
                            }

                            bleUartService!!.writeRXCharacteristic(dados)

                            if (posEnd == size) {
                                break
                            }

                            posBegin = posEnd
                            posEnd = posBegin + MAX_SIZE
                            if (posEnd > size) {
                                posEnd = size
                            }

                        } else {

                            break
                        }

                        // System.out.println("pos fim "  + posFim + " tam " + tam);

                        // Wait a while // workaround to Android BLE stuff //TODO Ver isto

                        try {
                            Thread.sleep(250)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    } while (posEnd <= size)
                }

            }.start()

        } catch (e: Exception) {
            this.activity.extShowException(e)
        }

        sending = false
    }

    // Connect to device

    private fun connectDevice(device: BluetoothDevice?) {

        try {

            // Is Bluetooth enabled ?

            if (!available) {

                adapter.enable()

                Thread.sleep(500) // TODO: ver isto
            }

            // Stop scan

            scanDevice(false)

            scanning = false
            connecting = true
            connectionFinished = false

            this.device = device

            // Callback to connecting

            if (bleUtilHandler != null) {
                bleUtilHandler!!.onConnecting()
            }

            logD("Attempting connect to device ${device!!.name} ${device.address}")


            // BT Low Energy

            // Execute on the Main Thread

            val handler = Handler(Looper.getMainLooper())
            handler.post { bleUartService!!.connect(this.device!!.address) }

            // Timeout for connection

            handlerTimeoutConnection = Handler()
            handler.postDelayed({

                //Did not connect? ?

                if (!deviceConnected) { // Cancel the handler was not ok  // TODO ver isto

                    // It indicates that it could not connect if the timeout occurs

                    timeoutConnection()

                }

                handlerTimeoutConnection = null

            }, this.timeoutConnection)

        } catch (e: Exception) {
            this.activity.extShowException(e)
        }
    }

    fun connectLastDevice() {

        // Connect to the last device because it is faster than discovery

        try {

            if (lastAddrConnected == null || // If there is no previously connected device
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) { // // or for Android 6 or lower

                // Search for device

                logD ("Last device does not exist, searching")

                scanDevice()

                return
            }

            // Only for Android 7.0 or later -> much faster than the scan :-)

            logD("Connecting to the last device:: ${lastAddrConnected!!}")

            val device = adapter.getRemoteDevice(lastAddrConnected) ?: return

            bleGreaterRSSI = -999 // Indicates direct connection

            // Connect

            connectDevice(device)

        } catch (e: Exception) {
            this.activity.extShowException(e)
        }
    }

    // Finalize the connection

    fun finalizeConnection() {

        if (connectionFinished)
            return

        connectionFinished = true
        connecting = false

        // BT Low Energy

        if (deviceConnected) {

            bleUartService!!.disconnect()

        } else {

            if (scanning) {
                scanDevice(false) // Stop searching if applicable
            }
        }

        if (bleUartService != null) {
            bleUartService!!.close() // TODO ???
        }

        if (handlerTimeoutConnection != null) { // Cancel connection timeout
            handlerTimeoutConnection!!.removeCallbacksAndMessages(null)
            handlerTimeoutConnection = null
        }

        // Zera variables

        deviceConnected = false
        scanning = false

    }

    // Turn off bluetooth

    fun turnOffBluetooth() {

        if (turnOffBluetooth) { // Only hang up if not previously connected

            bleUartService!!.close() // TODO ???

            adapter.disable()

            logD("desligado")
        }
    }

    // Receive data

    private fun receiveData(data: String) {

        receiveData(data.toByteArray())
    }

    private fun receiveData(data: ByteArray) {

        // Received Bluetooth data

        // logD(Util.expandStr(new String (data)));

        // Mark the time

        timeLastRecvData = System.currentTimeMillis()

        // Process bytes received

        for (byte in data) {

            if (byte.toInt() == 10) { //// New line

                // Line received

                logV("line recv: ${bufferLine.toString().extExpandStr()}")

                // Callback for receiving

                if (bleUtilHandler != null) {
                    bleUtilHandler!!.onReceiveLine(bufferLine.toString())
                }

                // Next

                bufferLine = StringBuffer()

            } else if (byte.toInt() != 13) {

                // Adds the received character

                bufferLine.append(byte.toChar())
            }
        }
    }

    /////////// Specifics of Bluetooth Low Energy

    // Inicialize

    private fun bleInitialize() {

        // ****** BLE

        try {

            // Device has BLE?

            if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                activity.extShowToast(strDeviceNotHaveBLE!!)
                activity.finish()
                return
            }

            // Initialize the Nordic Ble UART service

            // Execute on the Main Thread

            val handler = Handler(Looper.getMainLooper())
            handler.post { bleInitializeUartService() }

        } catch (e: Exception) {
            this.activity.extShowException(e)
        }

    }

    // Ble UART service connected / disconnected (based on Nordic example)

    private val bleUartServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, rawBinder: IBinder) {

            bleUartService = (rawBinder as BLEUartService.LocalBinder).service

            logD("service= ${bleUartService!!}")

            if (!bleUartService!!.initialize()) {
                activity.extAlert(strNotPossibleStartUARTBLEService!!)
                logD("Nao foi possivel initialize o servico BLE UART")
                activity.finish()
            }

            // Connection

            if (available) {

                initializeVariables()

                // Wait a while // TODO: ver isto

                try {
                    Thread.sleep(250)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                // Callback for connection

                if (bleUtilHandler != null) {
                    bleUtilHandler!!.onConnect()
                }

                // Connection with the last device

                nrAttemptConnection = 1 // Try 2x before going to the search

                connectLastDevice()

            } else {

                // Now this is done after starting BLE service

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) { // Android 5 ou inferior

                    // Ask to connect Bluetooth

                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

                } else { // recent Android -> Enable without prompting

                    // Enable Bluetooth

                    adapter.enable()

                    try {
                        Thread.sleep(1000) // TODO: ver isto
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Service disconnected ?

        override fun onServiceDisconnected(classname: ComponentName) {

            bleUartService = null

            // Abort the connection

            // Callback to abort connection

            if (bleUtilHandler != null) {

                if (!connectionFinished) {
                    bleUtilHandler!!.onAbortConnection(strDeviceHasDisconnected!!)
                }
            }
        }
    }

    // Status receiver

    private val bleUartStatusChangeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            //*********************//

            if (action == BLEUartService.ACTION_GATT_CONNECTED) {

                logD("Ble Uart -> Connected !!")

            }

            //*********************//

            if (action == BLEUartService.ACTION_GATT_DISCONNECTED) {

                logD("Ble Uart -> disconnected")

                // Only if it was not connecting

                if (!connecting) {

                    // End connection

                    val hasConnected = deviceConnected

                    finalizeConnection()

                    // Abort the connection

                    val message: String

                    // Disconnection?

                    if (hasConnected) {

                        message = strDeviceHasDisconnected!!

                    } else { // Nao conectou - time out

                        message = strNotPossibleConnDevice!!

                    }

                    // Callback to abort connection

                    if (bleUtilHandler != null) {
                        bleUtilHandler!!.onAbortConnection(message)
                    }

                } else {

                    // Try connecting again - Android buggggg or my ?? // TODO see this

                    logW ("Ble Uart: disconnected without being connected - trying again")

                    if (handlerTimeoutConnection != null) { // Cancela timeout da conexao
                        handlerTimeoutConnection!!.removeCallbacksAndMessages(null)
                        handlerTimeoutConnection = null
                    }

                    nrErrorsConnection++;

                    // Callback for try

                    if (bleUtilHandler != null) {
                        bleUtilHandler!!.onTryConnectAgain()
                    }

                }
            }

            //*********************//

            if (action == BLEUartService.ACTION_GATT_SERVICES_DISCOVERED) {

                logD ("Ble Uart -> Services discovered successfully !!")

                // Wait a while // TODO: ver isto

                try {
                    Thread.sleep(250)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                bleUartService!!.enableTXNotification()

                // Wait a while // TODO: ver isto

                try {
                    Thread.sleep(250)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                // Workaround -> device null

                if (device == null) {
                    if (bleDeviceCloser != null) {
                        device = bleDeviceCloser
                    } else {
//                        activity!!.extShowException(strBLEDeviceNull)
                        logE("onReceive: device BLE null")
                        return
                    }
                }

                // Successful connection

                deviceConnected = true
                connecting = false
                nrErrorsConnection = 0

                if (handlerTimeoutConnection != null) { // Cancela timeout da conexao
                    handlerTimeoutConnection!!.removeCallbacksAndMessages(null)
                    handlerTimeoutConnection = null
                }

                initializeVariables()

                logD("Connection OK")

                // Saves the address of the last connected device

                lastAddrConnected = device!!.address

                Preferences.save("BT_ADDR", lastAddrConnected!!)

                logD("Connected at device ${device!!.name} ${device!!.address}")

                // Callback for connected

                if (bleUtilHandler != null) {
                    bleUtilHandler!!.onConnected()
                }

            }

            //*********************//

            if (action == BLEUartService.ACTION_DATA_AVAILABLE) {

                val txValue = intent.getByteArrayExtra(BLEUartService.EXTRA_DATA)

                try {

                    // Received device data

                    val message= String(txValue)

                    if (debugExtra) {
                        logD("bleUartService: received -> $message")
                    }

                    receiveData(message)

                } catch (e: Exception) {
                    //activity!!.extShowException(e)
                    logE("onReceive")
                    e.printStackTrace()
                }

            }
            //*********************//
            if (action == BLEUartService.DEVICE_DOES_NOT_SUPPORT_UART) {
                activity!!.extShowException(strDeviceNotSupportUART)
                bleUartService!!.disconnect()
            }

        }
    }

    // Start Nordic Ble UART service

    private fun bleInitializeUartService() {

        logD("initializing .. $bleUartStatusChangeReceiver")

        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(bleUartStatusChangeReceiver)
            activity.unbindService(bleUartServiceConnection)
        } catch (e: Exception) {

        }

        val bindIntent = Intent(activity, BLEUartService::class.java)
        activity.bindService(bindIntent, bleUartServiceConnection, Context.BIND_AUTO_CREATE)

        LocalBroadcastManager.getInstance(activity).registerReceiver(bleUartStatusChangeReceiver,
                bleUartServiceMakeGattUpdateIntentFilter())

        BLEUartService.TAG = "EspApp_BleServ"

        // Wait a while

        Thread.sleep(500)

    }

    // Gatt

    private fun bleUartServiceMakeGattUpdateIntentFilter(): IntentFilter {

        val intentFilter = IntentFilter()
        intentFilter.addAction(BLEUartService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BLEUartService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BLEUartService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BLEUartService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BLEUartService.DEVICE_DOES_NOT_SUPPORT_UART)

        return intentFilter
    }

    // Scan to found a device

    private fun scanDevice(scan: Boolean) {


        try {

            // If there is a previous bleUtilHandler -> remove

            if (bleHandlerScan != null) {
                bleHandlerScan!!.removeCallbacksAndMessages(null)
                bleHandlerScan = null
            }

            // Scan ?

            if (scan) {

                // For Android 6 or higher you have to check if the gps is turned on to make the scanner

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    var locationMode = Settings.Secure.LOCATION_MODE_OFF
                    try {
                        locationMode = Settings.Secure.getInt(activity.applicationContext.contentResolver, Settings.Secure.LOCATION_MODE)
                    } catch (e: Settings.SettingNotFoundException) {
                        e.printStackTrace()
                    }

                    if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {

                        // Todo: Improve this - better and with confirmation screen

                        activity.extAlert(strTurnOnGPS!!) {

                            // Callback to abort connection

                            if (bleUtilHandler != null) {
                                bleUtilHandler!!.onAbortConnection(strGPSNotEnabled!!)
                            }

                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            activity.applicationContext.startActivity(intent)
                        }
                        return
                    }
                }

                scanning = true

                logD("scan")

                initializeVariables()

                // Callback for connection

                if (bleUtilHandler != null) {
                    bleUtilHandler!!.onConnect()
                }

                bleGreaterRSSI = -999   // To find the nearest
                bleDeviceCloser = null  // Indicates not found yet

                // Stop searching after a while

                bleHandlerScan = Handler()

                bleHandlerScan!!.postDelayed({
                    scanning = false

                    //TODO see it -> deprecated

                    adapter.stopLeScan(bleScanCallback)

                    logD("end of BLE scan")

                    // Return device

                    if (bleDeviceCloser != null) {

                        // To connect

                        logD("found device " +
                                bleDeviceCloser!!.name + "(" + bleDeviceCloser!!.address + ")")

                        device = bleDeviceCloser

                        connectDevice(device)

                    } else {

                        // Can not find

                        scanning = false

                        // Attempts

                        if (nrAttemptConnection == MAX_ATTEMPT_CONNECTION) {

                            // Callback to abort connection

                            if (bleUtilHandler != null) {
                                bleUtilHandler!!.onAbortConnection(strNotPossibleConnDevice!!)
                            }

                        } else {

                            // Search again

                            nrAttemptConnection++

                            scanDevice(true)

                        }
                    }
                }, BLE_TIME_SCAN)

                adapter.startLeScan(bleScanCallback)

            } else {

                if (scanning) {
                    logD("stop BLEscan")
                    adapter.stopLeScan(bleScanCallback)
                }
                scanning = false
            }

        } catch (e: Exception) {
            this.activity.extShowException(e)
        }

    }

    // Timeout on connection

    private fun timeoutConnection()  {

        val message = strNotPossibleConnDevice!!

        nrErrorsConnection++

        // Callback to abort connection

        if (bleUtilHandler != null) {
            bleUtilHandler!!.onAbortConnection(message)
        }
    }

    // BLE Scanner - TODO: make new Android method

    private val bleScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, _ ->
        if (device.name != null && device.name.startsWith(deviceName)) {

            if (debugExtra) {
                logD("btScanCallback: finded: ${device.name} rssi: $rssi")
            }

            // Last connected?

            if (lastAddrConnected != null && device.address == lastAddrConnected) { // Ultimo

                bleGreaterRSSI = 999 // // Prioritize the last
                bleDeviceCloser = device
                logD("last device founded! ${device.name} rssi $rssi")

            } else if (rssi > bleGreaterRSSI) { // Closer ?

                bleGreaterRSSI = rssi
                bleDeviceCloser = device
                logD("Closer device ${device.name} rssi $rssi")

            }
        }
    }
}

////// End