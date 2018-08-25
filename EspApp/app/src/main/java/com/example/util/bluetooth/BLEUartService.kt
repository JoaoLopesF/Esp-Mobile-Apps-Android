/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util/bluetooth
 * Comments  : BLEUartService - Service to BLE Uart - based on Nordic codes
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.bluetooth

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.util.UUID

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
class BLEUartService : Service() {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothDeviceAddress: String? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionState = STATE_DISCONNECTED

    var mtuChanged: Int = 0
        private set

    val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    val RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    val RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    val TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

    val BLE_MTU = 185 // MTU para Android 5.0+

//    val TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb")
//    val TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb")
//    val FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")
//    val DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                connectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                if (BLE.debugExtra) Log.i(TAG, "Connected to GATT server.")

                // If Android >= 5.0 - change MTU before discover services

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    // Request MTU - change it!

                    requestMTU()

                } else { // Request MTU not allowed -> discover services here

                    // Attempts to discover services after successful connection.

                    val ret = bluetoothGatt!!.discoverServices()
                    if (BLE.debugExtra) Log.i(TAG, "Attempting to start service discovery: $ret")

                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                connectionState = STATE_DISCONNECTED
                if (BLE.debugExtra) Log.i(TAG, "Disconnected from GATT server.")
                   broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (BLE.debugExtra) Log.w(TAG, "mBluetoothGatt = " + bluetoothGatt!!)

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                if (BLE.debugExtra) Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {

                mtuChanged = mtu - 3 // Menos os 3 bytes do BLE

                Log.w(TAG, "onMtuChanged: mtu changed to: $mtuChanged (mtu orig: $mtu)")

                // Discovery services after mtu changed

                val ret = bluetoothGatt!!.discoverServices()
                if (BLE.debugExtra) Log.i(TAG, "After mtu -> Attempting to start service discovery: $ret")
            }
        }
    }

    private val binder = LocalBinder()
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() = if (bluetoothGatt == null) null else bluetoothGatt!!.services

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is handling for the notification on TX Character of NUS service
        if (TX_CHAR_UUID == characteristic.uuid) {

            // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
            intent.putExtra(EXTRA_DATA, characteristic.value)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        internal val service: BLEUartService
            get() = this@BLEUartService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }
        // Previously connected device.  Try to reconnect.
        if (bluetoothDeviceAddress != null && address == bluetoothDeviceAddress
                && bluetoothGatt != null) {
            if (BLE.debugExtra) Log.d(TAG, "Trying to use an existing bluetoothGatt for connection.")
            if (bluetoothGatt!!.connect()) {
                connectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = bluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(this, false, gattCallback)

        if (BLE.debugExtra) Log.d(TAG, "Trying to create a new connection.")
        bluetoothDeviceAddress = address
        connectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.disconnect()
        // bluetoothGatt.close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (bluetoothGatt == null) {
            return
        }
        Log.w(TAG, "mBluetoothGatt closed")
        bluetoothDeviceAddress = null
        bluetoothGatt!!.close()
        bluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     *
     */

    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    fun enableTXNotification() {
        /*
    	if (bluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + bluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/

        val RxService = bluetoothGatt!!.getService(RX_SERVICE_UUID)
        if (RxService == null) {
            showMessage("Rx service not found!")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        val TxChar = RxService.getCharacteristic(TX_CHAR_UUID)
        if (TxChar == null) {
            showMessage("Tx charateristic not found!")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        bluetoothGatt!!.setCharacteristicNotification(TxChar, true)

        val descriptor = TxChar.getDescriptor(CCCD)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt!!.writeDescriptor(descriptor)

    }

    fun writeRXCharacteristic(value: ByteArray) {

        if (bluetoothGatt == null) {
            showMessage("bluetoothGatt null")
            return
        }

        val RxService = bluetoothGatt!!.getService(RX_SERVICE_UUID)
        if (BLE.debugExtra) showMessage("mBluetoothGatt " + bluetoothGatt!!)
        if (RxService == null) {
            showMessage("Rx service not found!")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        val RxChar = RxService.getCharacteristic(RX_CHAR_UUID)
        if (RxChar == null) {
            showMessage("Rx charateristic not found!")
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART)
            return
        }
        RxChar.value = value
        val status = bluetoothGatt!!.writeCharacteristic(RxChar)

        if (BLE.debugExtra) Log.d(TAG, "write TXchar - status=$status")
    }

    private fun showMessage(msg: String) {
        Log.e(TAG, msg)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun requestMTU() {

        val mtu = BLE_MTU + 3 // Maximum allowed 517 - 3 bytes of BLE

        // Request MTU to max allowed

        bluetoothGatt!!.requestMtu(mtu)

        Log.w(TAG, "requestMTU -> mtu=$mtu")
    }

    // Static

    companion object {

        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2

        const val ACTION_GATT_CONNECTED = "UART.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "UART.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "UART.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "UART.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "UART.EXTRA_DATA"
        const val DEVICE_DOES_NOT_SUPPORT_UART = "UART.DEVICE_DOES_NOT_SUPPORT_UART"

        var TAG = BLEUartService::class.java.simpleName!!

    }

}
