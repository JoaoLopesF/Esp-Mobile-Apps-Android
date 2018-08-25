/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : MainActivity - main activify in app
 * Comments  :
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 *
 */

/*
 * TODO list:
 *
 * - Remove all warnings
 */

package com.example.espapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.example.espapp.BuildConfig
import com.example.espapp.R
import com.example.espapp.helpers.AppSettings
import com.example.espapp.helpers.MessagesBLE
import com.example.espapp.models.BLEDebug
import com.example.util.*
import com.example.util.bluetooth.BLEHandler
import com.example.util.bluetooth.BLE
import com.example.util.extentions.*
import java.util.*

////// MainActivity

class MainActivity : FragmentActivity() {

    //////// Companion (static)

    companion object {

        // Back pressed

        const val BACK_PRESSED_DELAY = 2000

    }

    //////// Views

    // --- Connection

    lateinit var fragmentConnection: FragmentConnection               // Fragment for connection/disconnection BLE
        private set

    // --- Main menu

    lateinit var fragmentMainMenu: FragmentMainMenu                   // Fragment of main menu
        private set

    // --- Informations about ESP32

    lateinit var fragmentInfoESP32: FragmentInfoESP32                   // Fragment of ESP32 informations
        private set

    // --- Terminal BLE

    lateinit var fragmentTerminalBLE: FragmentTerminalBLE             // Fragment of terminal BLE
        private set

    // Dialog

    var alertDialog: AlertDialog? = null                                // To show alerts

    // View main activity (Common views for all fragments)

    private var textViewTitle: TextView? = null                         // Title
    private var textViewSubTitle: TextView? = null                      // Subtitle
    private var buttonExit: Button? = null                              // Exit button

    private var textViewStatus: TextView? = null                        // Status message
    private var imageViewStatusBT: ImageView? = null                    // Status BLE image
    private var imageViewStatusEnergy: ImageView? = null                // Status image of energy(battery, etc.)
    private var textViewStatusEnergy: TextView? = null                 // Status message of energy

    // Drawables

    private var drawableIconBT: Drawable? = null                        // To show in status bar
    private var drawableIconBTInactive: Drawable? = null

    ///////// Timers

    private val timer = Timer()                                         // For timers
    private var timerSecondsActive: Boolean = false
    private var timerTaskSeconds: TimerTask? = null
    private var timerRunSeconds: Runnable? = null
    private val timerHandler = Handler()
    private val timerStatusHandler = Handler()

    ///////// Back treatment

    private var backPressedOnce = false
    private val backPressedHandler = Handler()

    private val backPressedTimeoutAction = Runnable { backPressedOnce = false }

    ///////// Variables

    // Versions

    private var versionApp = ""                                     // Version of app (get it from Android)
    private var versionDevice: String? = null                       // Version of BLE device firmware

    var sendFeedback = false                                        // Send periodic feedback BLE messages

    var timeSeconds: Int = 0                                        // To count time
        private set

    private var timeFeedback: Int = 0                               // Feedbacks time

    private var exitingApp: Boolean = false                         // Exiting from app ?

    // Handler for exceptions not treateds

    private var exceptionHandler: ExceptionHandler? = null

    // --- Bluetooth low Energy

    lateinit var ble: BLE                                           // BLE class
        private set
    
    lateinit var bleDebugs: MutableList<BLEDebug>                   // List of debug to show in Terminal BLE
        private set

    var bleDebugEnabled: Boolean = false                            // Enabled ?

    private var bleStatusActive = 0                                 // Status of BLE is active ?

    private var bleWaitingResponse = false                          // Waiting for response ?

    private var bleAbortingConnection = false                       // Aborting connection ?

    private var bleTimeout = 0                                      // Time counter for timeout
    private var bleVerifTimeout = false                             // Verify timeouts ?

    private var bleLastTimeAbortConn: Long = 0                      // Last time of abort connection

    var modeDemo: Boolean = false                                   // Demo mode - without Bluetooth
        private set

    var fragmentPrevious = ""                                       // Name of previous fragment

    var timeActive = 0                                              // Time for inactivity checks

    var envDevelopment: Boolean = false                             // Development environment ?
        private set

    // Do device

    var deviceHaveBattery: Boolean = false       // Device connected have a battery
        private set
    var deviceHaveSenCharging: Boolean = false   // Device connected have a sensor of charging of battery
        private set
    var poweredExternal: Boolean = false         // Powered by external (USB or power supply)?
        private set
    var chargingBattery: Boolean = false         // Charging battery ?
        private set
    var statusBattery:String = "100%"            // Battery status
        private set
    var voltageBattery: Float = 0.0f            // Voltage calculated of battery
        private set
    var readADCBattery: Int = 0                 // Value of ADC read of battery
        private set

    /////// Proprieties

    var fragmentActual: String = ""
        set(nome) {

            logI("Antes -> fragmentActual: $fragmentActual fragmentPrevious: $fragmentPrevious")

            // Save previous before

            fragmentPrevious = fragmentActual

            // Set name do fragment actual

            field = nome

            logI("Depois -> fragmentActual: $fragmentActual fragmentPrevious: $fragmentPrevious")

        }

    //////// Events

    // On create

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ****** Initialize

        // USB connected ?

        val isUSBconnected = Util.isUSBConnected(this)

        // Development environment - app not installed in the virtual store?

        if (isUSBconnected) {
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                envDevelopment = (BuildConfig.DEBUG || packageManager.getInstallerPackageName(applicationInfo.packageName) != "com.android.vending")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            envDevelopment = false
        }

        // Global error handling - very good for catching unhandled errors

        exceptionHandler = ExceptionHandler(this)

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)

        // Version of this app (get it from Android)

        try {
             versionApp = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        // Initialize logs

        logTag = "EspApp"
        logActive = envDevelopment

        // Initialize preferences

        Preferences.initialize(applicationContext)

        // Debug

        logD("--- onCreate")

        // Previous state saved

        if (savedInstanceState != null) {

            logD("savedInstanceState != NULL !")

        } else {

            // ****** View

            setContentView(R.layout.activity_main)

        }

        // Initialize (after a time to display splash)

        val handler = Handler()
        handler.postDelayed({
            // Initialize App
            initializeApp()
        }, 1000)

    }

    // On start

    public override fun onStart() {
        super.onStart()
        logD("--- onStart")
    }

    // On resume

    @Synchronized
    public override fun onResume() {
        super.onResume()
        // Todo see logic about this
        logD("--- onResume")
//        if (!btAdapter.isEnabled()) {
//            logD("onResume - BT not enabled yet");
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }
    }

    // On pause

    @Synchronized
    public override fun onPause() {
        super.onPause()
        logD("--- onPause")
    }

    // On stop

    public override fun onStop() {
        super.onStop()
        // Todo see logic about this
        logD("--- onStop")

    }

    // Save instance

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        // No call for super(). Bug on API Level > 11.
    }

    // On destroy

    public override fun onDestroy() {

        super.onDestroy()

        // Todo see logic about this

        logD("--- onDestroy")

        backPressedHandler.removeCallbacks(backPressedTimeoutAction)

        // Desactivate timer

        activateTimer(false)

        // Send a message

        if (AppSettings.TURN_OFF_DEVICE_ON_EXIT) {

            if (::ble.isInitialized && ble.deviceConnected) {
                bleSendMessage(MessagesBLE.MESSAGE_STANDBY, false)
            }
        }

        // Wait a time

        Thread.sleep(500)

        // Finalize Bluetooth

        if (::ble.isInitialized) {
            ble.finalize()
        }

        // Power save mode, turn bluetooth off before exiting

        if (AppSettings.modePowerSave && ::ble.isInitialized) {

            ble.turnOffBluetooth()
        }

        // End the application - only if it finishes or is not in development (Instant Play causes this)

        if (isFinishing || !envDevelopment) {
            Util.abortActivity(this)
        }
    }

    // Treat return of Bluetooth activation request or other

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        try {

            logD("$resultCode")

            if (requestCode == BLE.REQUEST_ENABLE_BT) {

                if (resultCode == Activity.RESULT_OK) {

                    ble.treatOnActivityResultOk(requestCode)

                } else {

                    // Denied access to Bluetooth

                    this.extShowToast(getString(R.string.bt_not_enabled))
                    finish()

                }
                return
            }
            return  // false;

        } catch (e: Exception) {
            this.extShowException(e)
            return
        }
    }

    // Back pressed - logic to 2 back to exit

    override fun onBackPressed() {

        try {

            if ((fragmentActual == "MainMenu" || fragmentActual == "Connection") || // Fragment actual
                    fragmentPrevious == "Connection") {                             // Fragment previous

                if (this.backPressedOnce) {

                    // End the application

                    finalize()

                    return
                }

                // Press again message

                this.backPressedOnce = true

                this.extShowToast(getString(R.string.back_to_exit))

                backPressedHandler.postDelayed(backPressedTimeoutAction, BACK_PRESSED_DELAY.toLong())

            } else {

                try {
                    super.onBackPressed()
                } catch (e: Exception) {
                    logE("", e)
                }
            }

        } catch (e: Exception) {
            this.extShowException(e)
        }

    }

    // Initialize App

    private fun initializeApp() {

        try {

            logD("*** Initializing")

            // Screen Density

            val displayMetrics = resources.displayMetrics
            val dpHeight = displayMetrics.heightPixels / displayMetrics.density
            val dpWidth = displayMetrics.widthPixels / displayMetrics.density

            logV("*** Screen resolution: ${displayMetrics.widthPixels} x ${displayMetrics.heightPixels}(${dpWidth}dp x ${dpHeight}dp)")

            // Date and time format

            FormatterUtil.formatDateTimeLocale = getString(R.string.format_date_time)

            FormatterUtil.formatTimeLocale = getString(R.string.format_time)

            // Initialize module variables

            initializeVariables()

            // Initialize screen

            initializeScreen()

            // Clear status message

            showMessageStatus("")

            // Read preferences

            readPreferences()

            // Initialize BLE

            if (!Util.isEmulator) {

                // Activate debug ? (only if have terminal)

                bleDebugEnabled = AppSettings.TERMINAL_BLE

                // Note: Is important pass string to BLE, to this stay independent of project
                // More easy in futures updates (just download and update just Util directory

                // Instance the BLE class

                ble = BLE(this,
                        AppSettings.BT_NAME_DEVICE,
                        getString(R.string.bt_not_avaliable),
                        getString(R.string.bt_device_has_disconnected),
                        getString(R.string.bt_device_not_connected),
                        getString(R.string.ble_uart_service_not_active),
                        getString(R.string.ble_device_not_have),
                        getString(R.string.bt_service_ble_uart_not_init),
                        getString(R.string.ble_device_null),
                        getString(R.string.ble_uart_not_supported),
                        getString(R.string.bt_turn_on_gps),
                        getString(R.string.bt_gps_not_activate),
                        object : BLEHandler {

                            override fun onConnect() {

                                // Connection

                                bleProcessConnection(null)

                            }

                            override fun onAbortConnection(message: String) {

                                // Abort connection

                                bleAbortConnection(message)

                            }

                            override fun onConnecting() {

                                // Connecting

                                textViewStatusEnergy!!.text = ""

                                val message = "${getString(R.string.device_connecting)} ${ble.device!!.address} ..."
                                showMessageStatus(message)

                                // Debug

                                if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

                                    bleAddDebug('C', message) // Add debug
                                }

                            }

                            override fun onConnected() {

                                // Connected

                                // Process connection and show main menu

                                bleProcessConnection(null)

                                // Activate timer

                                activateTimer(true)

                                // Send an initial message

                                bleSendMessage(MessagesBLE.MESSAGE_INITIAL, true, getString(R.string.ext_initial))

                            }

                            override fun onTryConnectAgain() {

                                // Try new connection - to treat disconnect bug without connecting

                                bleTryConnectAgain()
                            }

                            override fun onReceiveLine(line: String) {

                                // Received line (message)

                                bleReceiveLine(line)

                            }
                        })


            } else { // Emulator (without Bluetooth)

                modeDemo = true
            }

            // Displays the connection screen

            bleProcessConnection(null)


        } catch (e: Exception) {
            this.extShowException(e)
        }

    }

    // Read app preferences

    private fun readPreferences() {

        // Example

        //prefer = Preferences.read("name", 'S')

    }

    // Return to previous fragment

    fun returnFragmentPrevious() {

        try {

            val fragmentTransaction = supportFragmentManager.beginTransaction()

            logD("actual -> $fragmentActual previous -> $fragmentPrevious")

            if (fragmentPrevious == "MainMenu") {

                fragmentTransaction.replace(R.id.fragment, fragmentMainMenu)

//            } else if (fragmentPrevious == "xxxxx") {
//
            }

            fragmentTransaction.commit()

        } catch (e: Exception) {
            this.extShowException(e)
        }
    }


    // *** Routines

    // Initialize the screen

    private fun initializeScreen() {

        // Set variable for the screen

        try {

            logD()

            // Fragments

            fragmentConnection = FragmentConnection()
            fragmentConnection.mainActivity = this

            fragmentMainMenu = FragmentMainMenu()
            fragmentMainMenu.mainActivity = this

            fragmentInfoESP32 = FragmentInfoESP32()
            fragmentInfoESP32.mainActivity = this

            fragmentTerminalBLE = FragmentTerminalBLE()
            fragmentTerminalBLE.mainActivity = this

            // For app header

            textViewTitle = findViewById<View>(R.id.textViewTitle) as TextView
            textViewSubTitle = findViewById<View>(R.id.textViewSubTitle) as TextView

            // For exit button

            buttonExit = findViewById<View>(R.id.buttonExit) as Button
            buttonExit!!.setOnClickListener {

                // Confirm to exit

                extConfirm(getString(R.string.confirm_exit_app), { Util.finalizeApp() })
            }

            // For statusbar

            textViewStatus = findViewById<View>(R.id.textViewStatus) as TextView
            imageViewStatusBT = findViewById<View>(R.id.imageViewStatusBT) as ImageView
            imageViewStatusEnergy = findViewById<View>(R.id.imageViewStatusEnergy) as ImageView
            textViewStatusEnergy = findViewById<View>(R.id.textViewStatusEnergy) as TextView

            // Icon of BT

            drawableIconBT = ContextCompat.getDrawable(this, R.drawable.bt_icon)
            drawableIconBTInactive = ContextCompat.getDrawable(this, R.drawable.bt_icon_inactive)

            // Uncomment it if want screen should remain on

            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Displays version

            exibirSubTitle()

        } catch (e: Exception) {
            this.extShowException(e)
        }

    }

    // Exit from app

    private fun finalize() {

        try {

            exitingApp = true

            // Wait a while

            Thread.sleep(500)

            // Finalize BT connection

            if (::ble.isInitialized) {
                ble.finalize()
            }

        } catch (e: Exception) {
            logE("finalize", e)
        } finally {

            // Finalize app

            finish()

        }
    }

    // Displays the subtitle

    private fun exibirSubTitle() {

        if (textViewSubTitle != null) {

            runOnUiThread {
                try {

                    val versionDevice: String =
                        if (modeDemo) "Demo"
                        else if (versionDevice != null) getString(R.string.subtitle_device) + versionDevice!!
                        else "..."

                    textViewSubTitle!!.text = "App.: $versionApp\n$versionDevice"

                } catch (e: Exception) {
                    this.extShowException(e)
                }
            }

        }
    }

    // Check the status of the Bluetooth connection

    private fun bleProcessConnection(messageError: String?) {

        // Displays the connection screen or the main menu according to the connection

        logD()

        runOnUiThread {
            try {

                // Show the fragment

                val fragmentTransaction = supportFragmentManager.beginTransaction()

                // It is showing an error !!!!
                // this.supportFragmentManager.popBackStack (null, FragmentManager.POP_BACK_STACK_INCLUSIVE) // Close all fragments before

                if (modeDemo || ble.deviceConnected) { // Connected or in demonstration (or emulator)

                    // Subtitle

                    exibirSubTitle()

                    // Displays the main menu

                    fragmentTransaction.replace(R.id.fragment, fragmentMainMenu)


                    // Send feedbacks

                    sendFeedback = true

                    // Real device

                    if (::ble.isInitialized) {

                        val message = "${getString(R.string.device_connected)} ${ble.device!!.address}"

                        showMessageStatus(message, 2000)

                        // Debug

                        if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

                            bleAddDebug('C', message) // Add debug
                        }

                    }

                } else {

                    // Displays the connection screen

                    setTitle(getString(R.string.connecting))

                    exibirSubTitle()

                    fragmentTransaction.replace(R.id.fragment, fragmentConnection)

                    // There was a connection error?

                    if (messageError == null) { // There was no, it displays the connection screen

                        fragmentConnection.showConnecting(ble.scanning)

                    } else { // There was an error, it displays the error screen

                        fragmentConnection.showConnectionError(messageError)
                        showMessageStatus(messageError)

                    }

                    // Not send feedbacks

                    sendFeedback = false

                    // Debug

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

                        bleAddDebug('D', getString(R.string.bt_device_has_disconnected)) // Add debug
                    }
                }

                fragmentTransaction.commitAllowingStateLoss()

            } catch (e: Exception) {
                this.extShowException(e)
            }
        }
    }

    // Aborts processing, connection, etc.

    fun bleAbortConnection(message: String) {

        try {

            if (bleAbortingConnection) // To avoid messages in loop
                return

            if (bleLastTimeAbortConn > 0) {
                val tempo = (System.currentTimeMillis() - bleLastTimeAbortConn) / 1000
                if (tempo < 15) // To avoid running again when the connection fails
                    return
            }

            // Abort

            bleAbortingConnection = true
            bleLastTimeAbortConn = System.currentTimeMillis()

            logD("msg=$message")

            // Send message

            if (ble.deviceConnected) {
                bleSendMessage(if (!chargingBattery) MessagesBLE.MESSAGE_STANDBY else MessagesBLE.MESSAGE_RESTART, false)
            }

            // Abort timers

            activateTimer(false)

            // Hide dialog

            try {

                if (alertDialog != null) {
                    alertDialog!!.cancel()
                    alertDialog!!.hide()
                }

            } catch (e: Exception) {
                logE("exception", e)
            }

            // Finalize connection Bluetooth

            ble.finalizeConnection()

            // Initalizae variables

            initializeVariables()

            // Debug

            if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                bleAddDebug('O', "${getString(R.string.connection_aborted)}  $message") // Add debug
            }

            // Displays connection screen (with connection error or not)

            bleProcessConnection(message)

            // Indicates end of this routine

            bleAbortingConnection = false

        } catch (e: Exception) {
            this.extShowException(e)
        }
    }

    // send an bluetooth message

    fun bleSendMessage(message: String, waitResponse: Boolean, debugExtra: String = "") {

        @Suppress("NAME_SHADOWING")
        var message = message

        try {

            if (modeDemo || !ble.deviceConnected) {
                return
            }

            logV("")

            // Reinitialize the time for feedbacks

            timeFeedback = 0

            // Debug

            if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

                val debug = message.extExpandStr()

                bleAddDebug('S', message, debugExtra) // Add debug
            }

            // Message without new line?

            if (message.indexOf('\n') == -1) {
                message += '\n'
            }

            // Show message

            showMessageStatus(getString(R.string.sending_message_bluetooth))

            // Send a message by BLE

            ble.sendMessage(message)

            // Wait a response ?

            if (waitResponse) {

                // Wait for response -> active timeout for this return

                bleWaitingResponse = true

                bleTimeout = AppSettings.BT_TIMEOUT
                bleVerifTimeout = true

                showMessageStatus(getString(R.string.bt_waiting_response))

            } else {

                // No waiting for message -> No timeout

                bleWaitingResponse = false

                bleTimeout = 0
                bleVerifTimeout = false

                showMessageStatus("", 250)

            }

            bleShowIconActive(true)

        } catch (e: Exception) {
            this.extShowException(e)
        }

    }

    // Displays the BT icon, active or not

    private fun bleShowIconActive(active: Boolean) {

        bleStatusActive = if (active) 2 else 0 // For 2 seconds to give time to see ;-)

        this@MainActivity.runOnUiThread {
            if (imageViewStatusBT != null) {
                imageViewStatusBT!!.setImageDrawable(if (active) drawableIconBT else drawableIconBTInactive)
            }
        }
    }

    // Send feedback

    private fun bleSendFeedback() {

        if (modeDemo) {
            return
        }

        try {

            logV("")

            // If you are sending, ignore

            if (ble.sending) {
                return
            }

            // Send message

            showMessageStatus(getString(R.string.sending_feedback))

            bleSendMessage(MessagesBLE.MESSAGE_FEEDBACK, true, getString(R.string.ext_feedback))

        } catch (e: Exception) {
            this.extShowException(e)
        }
    }

    // Received Bluetooth data (line)

    private fun bleReceiveLine(line: String) {

        // Debug

        logD("line-> $line")

        if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

            bleAddDebug('R', line) // Add debug
        }

        // Disable timeout checking

        bleVerifTimeout = false

        // Restart the feedback time

        timeFeedback = 0

        // Process the message

        if (line.startsWith(MessagesBLE.MESSAGE_ERROR)) {

            // Error occurred

            var message: String = getString(R.string.exception_in_app);
            message += line

            this.extShowToast(message, true)

            bleAbortConnection(message)
            return

        } else {

            // Received reply

            if (bleWaitingResponse) { // Waiting ...

                showMessageStatus("", 250)
            }
        }

        // Process the received message

        bleProcessMessageRecv(line)

    }

    // Process the received message

    private fun bleProcessMessageRecv(message: String) {

        @Suppress("NAME_SHADOWING")
        var message = message

        // Check the messages

        try {

            // Valid ?

            if (message.length < 3) {

                this.extShowToast(getString(R.string.receive_wrong_message))

                if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

                    bleAddDebug('O', getString(R.string.receive_wrong_message)) // Add debug
                }

                logD("invalid msg: $message")

                // Abort processing - uncomment to do it
                // bleAbortConnection(getString(R.string.receive_wrong_message));

                return

            }

            // Extract the delimited fields

            val fields: Fields = Fields(message, ":")

            // Extract code

            val codMsg:Int = Util.convStrInt(fields.getField(1))

            // Restart the timeout

            bleTimeout = AppSettings.BT_TIMEOUT

            // Process the messages

            when (codMsg) {

                MessagesBLE.CODE_OK // OK
                -> {
                    // message OK

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug("Ok")
                    }

                }

                MessagesBLE.CODE_INITIAL // Initial
                -> {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_initial))
                    }

                    // Processes the return

                    bleProcessInitial(fields)
                }

                MessagesBLE.CODE_ENERGY // Power status: extern (USB, etc.) or battery
                -> {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_energy))
                    }

                    logV ("Power message received -> $ message")

                    bleProcessEnergy(fields)

                    // Note: example of call subroutine to update the screen

                    if (fragmentActual == "InfoESP32") {
                        fragmentInfoESP32.updateEnergyInfo()
                    }

                }

                MessagesBLE.CODE_INFO// Status of energy: USB ou Battery
                -> {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_info))
                    }

                    logV("Message of info")

                    bleProcessInfo(fields)


                }

                MessagesBLE.CODE_ECHO // Debug - echo
                -> {

                    // Echo received

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_echo))
                    }

                    if (fragmentActual == "TerminalBLE") {

                        // Is to send repeated (only for echoes) ??

                        if (fragmentTerminalBLE.repeatSend) {

                            fragmentTerminalBLE.bleTotRepeatPSec++

                            bleSendMessage(message, true, getString(R.string.ext_echo))

                        }
                    }
                }

                MessagesBLE.CODE_FEEDBACK // Feedback
                -> {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_feedback))
                    }

                }

                MessagesBLE.CODE_STANDBY // Device have entered standby
                -> {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_standby))
                    }


                    bleAbortConnection(getString(R.string.device_has_off))

                    this.extShowToast(getString(R.string.device_has_off))
                }

                else // Unknown code
                -> {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?
                        bleUpdateDebug( getString(R.string.ext_invalid))
                    }

                    logD("abort - msg invalid $message")
                }
            }

        } catch (e: Exception) {

            this.extShowException(e)
        }

    }

    // Process initial message

    private fun bleProcessInitial(fields: Fields) {

        // Note: this is a example how this app discovery device hardware
        // This usefull to works with versions or models of hardware
        // For example: if device have a battery, the app show status of this

        // Format of message: 01:FIRWARE VERSION:HAVE BATTERY:HAVE SENSOR CHARGING

        logV("initial msg")

        // Version

        versionDevice = fields.getNextField()

        logV("version of device $versionDevice")

        // Have a battery

        deviceHaveBattery = ( fields.getNextField() == "Y")

        // Have a battery charging sensor

        deviceHaveSenCharging = ( fields.getNextField() == "Y")

        // Show/hide battery in screen

        showStatusBattery()

        // Show it

        exibirSubTitle()

        this.extShowToast(getString(R.string.device_connected_version) + " " + versionDevice)

    }

    // Process energy message

    private fun bleProcessEnergy(fields: Fields) {

        // Format of message: 10:POWERED:CHARGING:ADC_BATTERY

        poweredExternal = (fields.getNextField() == "EXT")
        chargingBattery = (fields.getNextField() == "Y")
        readADCBattery = fields.getNextFieldInt()

        logV("usb=$poweredExternal charging=$chargingBattery vbat=$readADCBattery")

        // Calculate the voltage (done here note in firmware - due more easy to update)
        // TODO: see it! please caliber it first !
        // To caliber:
        //  - A charged battery plugged
        //  - Unplug the USB cable (or energy cable)
        //  - Meter the voltage of battery with multimeter
        //  - See the value of ADC read in monitor serial or in App informations

        val voltageCaliber: Float = 3.942f
        val readADCCaliber: Float = 3168f
        val factorCaliber: Float = (voltageCaliber / readADCCaliber)

        // Voltage readed from ADC

        val oldVoltage: Float = voltageBattery

        voltageBattery = Util.round((readADCBattery * factorCaliber), 2)

        logV("vbat -> ${voltageBattery}v")

        // Calculate the %

        var percent:Int = 0
        var voltage:Float = voltageBattery

        if (voltage >= 2.5f) {
            voltage -= 2.5f // Limit  // TODO: see it!
            percent = Util.round(((voltage * 100.0f) / 1.7f), 0).toInt()
            if (percent > 100) {
                percent = 100
            }
        } else {
            percent = 0
        }

        // Show o icon of battery and percent of this
        // TODO: see it! Experimental code, please verify this works ok

        var drawableIcon: Drawable? = null

        statusBattery = ""

        if (deviceHaveSenCharging) { // With sensor of charging

            if (poweredExternal) { // Powered by USB or external

                statusBattery = getString(R.string.bat_status_ext)

            } else {

                statusBattery = getString(R.string.bat_status_bat)

            }

            if (poweredExternal && chargingBattery) { // Charging by USB or external

                drawableIcon = ContextCompat.getDrawable(this, R.drawable.charging)

                statusBattery += getString(R.string.bat_status_chg)

            } else { // Not charging, process a voltage

                statusBattery += "|$percent%"

            }

        } else { //without sensor

            if (poweredExternal) { // Powered by USB or external

                drawableIcon = ContextCompat.getDrawable(this, R.drawable.charging)
                statusBattery = "Ext"

            } else { // Not charging, process a voltage

                statusBattery = "$percent%"

            }

        }

        // In UI thread

        runOnUiThread {
            try {

                // show image of battery by voltage ?

                if (drawableIcon == null ) {

                    // Set image by voltage

                    when {
                        voltageBattery >= 4.2f -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery6)
                        voltageBattery >= 3.9f -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery6)
                        voltageBattery >= 3.7f -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery5)
                        voltageBattery >= 3.5f -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery4)
                        voltageBattery >= 3.3f -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery3)
                        voltageBattery >= 3.0f -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery2)
                        else -> drawableIcon = ContextCompat.getDrawable(this, R.drawable.battery1)
                    }

                }

                // Show it

                imageViewStatusEnergy!!.setImageDrawable(drawableIcon!!)
                textViewStatusEnergy!!.text = statusBattery

                // Battery low ?

                val low: Float = 3.1f // Experimental // TODO do setting

                if (voltageBattery <= low &&
                        (oldVoltage == 0.0f || oldVoltage > low)) {

                    extAlert(getString(R.string.attention_low_battery))
                }

            } catch (e: Exception) {
                this.extShowException(e)
            }
        }
    }

    // Display battery status ?

    private fun showStatusBattery() {

        runOnUiThread {
            try {

                if (imageViewStatusEnergy != null) {

                    imageViewStatusEnergy!!.visibility = (if (deviceHaveBattery) VISIBLE else GONE)
                    textViewStatusEnergy!!.visibility = (if (deviceHaveBattery) VISIBLE else GONE)

                }

            } catch (e: Exception) {
                this.extShowException(e)
            }
        }
    }

    // Process info messages

    fun bleProcessInfo(fields: Fields) {

        // Example of process content delimited of message
        // Note: field 1 is a code of message

        // Is on informations screen ?
        // Note: example of show data in specific view controller

        if (fragmentActual != "InfoESP32") {
            return
        }

        // Extract data

        val type: String = fields.getNextField()
        var info: String = fields.getNextField()

        logV("type: $type info: $info")

        // In UI thread

        runOnUiThread {
            try {

                // Process information by type
                // VUSB and VBAT is by energy type message

                when (type) {

                    "ESP32"
                    -> {
                        // About ESP32

                        // Works with info (\n (message separator) and : (field separator) cannot be send by device

                        info = info.replace('#', '\n') // replace it
                        info = info.replace(';', ':') // replace it

                        if (!Util.isEmulator) { // Device real
                            info += "* RSSI of connection: "
                            info += (if (ble.bleGreaterRSSI != -999) ble.bleGreaterRSSI else "?")
                            info += '\n'
                        }
                        info += "*** Device hardware"
                        info += '\n'
                        info += "* Have a battery ?: "
                        info += (if (deviceHaveBattery) "Yes" else "No")
                        info += '\n'
                        info += "* Have sensor charging ?: "
                        info += (if (deviceHaveSenCharging) "Yes" else "No")
                        info += '\n'

                        fragmentInfoESP32.setInfoESP32 (info, "INFO_ESP32")
                    }
                    "VDD33"
                    -> {

                        // Voltage reading of ESP32 - Experimental code!
                        // Calculate the voltage (done here note in firmware - due more easy to update)
                        // TODO: see it! please caliber it first !
                        // To caliber:
                        //  - Unplug the USB cable (or energy cable)
                        //  - Meter the voltage of 3V3 pin (or 2 pin of ESP32)
                        //  - See the value of rom_phy_get_vdd33 read in monitor serial or in App informations

                        val voltageCaliber: Float = 3.317f
                        val readPhyCaliber: Float = 6742f
                        val factorCaliber: Float = (voltageCaliber / readPhyCaliber)

                        // Voltage readed from ADC

                        val voltageEsp32: Float = Util.round((info.toFloat() * factorCaliber), 2)

                        fragmentInfoESP32.setInfoESP32("$info (${voltageEsp32}v)", "VOLT_ESP32")
                    }
                    "FMEM"
                    -> {
                        // Free memory of ESP32

                        fragmentInfoESP32.setInfoESP32(info, "FREE_MEM_ESP32")

                    }
                    else // Unknown
                    -> {
                        logE("Invalid type: $type")
                    }
                }

            } catch (e: Exception) {
                this.extShowException(e)
            }
        }
    }

    // Initialize control variables

    fun initializeVariables() {

        bleWaitingResponse = false
        bleVerifTimeout = false
        bleTimeout = 0

        sendFeedback = false

        timeActive = AppSettings.TIME_MAX_INACTITIVY

        bleDebugs = mutableListOf()

    }

    // Activate timer for every second ?

    fun activateTimer(activate: Boolean) {

        logD("activate $activate")

        try {

            timeSeconds = 0
            timeFeedback = 0

            if (activate) {

                if (timerSecondsActive) {

                    // Disable before the previous one

                    timerTaskSeconds!!.cancel()
                    timerHandler.removeCallbacks(timerRunSeconds)
                    timerRunSeconds = null

                }

                // Activate timer

                timerSecondsActive = true

                timerTaskSeconds = object : TimerTask() {

                    override fun run() {

                        timerRunSeconds = Runnable {
                            if (timerSecondsActive) {

                                timerTickSeconds()

                            } else {

                                logD("Cancel timer")

                                cancel()
                            }
                        }

                        timerHandler.post(timerRunSeconds)
                    }
                }

                timer.schedule(timerTaskSeconds, 1000, 1000) // uma vez por segundo

            } else {

                // Cancel timer

                if (timerSecondsActive) {
                    timerTaskSeconds!!.cancel()
                    timerHandler.removeCallbacks(timerRunSeconds)
                    timerRunSeconds = null
                }

                timerSecondsActive = false
            }

        } catch (e: Exception) {

            this.extShowException(e)

        }
    }

    // Timer tick every second

    private fun timerTickSeconds() {

        try {

            // Check the connection

            if (!modeDemo && !ble.deviceConnected) {

                // Check the timer

                if (timerSecondsActive) {

                    logD("Deactivate timer - BLE disconnected")

                    activateTimer(false)

                    return
                }
            }

            // Send Timeout

            if (bleVerifTimeout && !envDevelopment) { // Not for development

                bleTimeout--

                if (bleTimeout <= 0) {

                    if (AppSettings.TERMINAL_BLE && bleDebugEnabled) { // App have a Terminal BLE (debug) and it is enabled ?

                        bleAddDebug('O', "*** Timeout") // Add debug
                    }

                    logD("Timer - timeout")

                    bleAbortConnection(getString(R.string.without_response_device))

                    return
                }
            }

            // Count time

            timeSeconds++

            // Check inactivity

            if (fragmentActual == "MainMenu") { // Only for main menu

                // Remaining time to go into inactivity

                timeActive--

                if (timeActive <= 0) {

                    // Abort connection

                    bleAbortConnection(getString(R.string.reached_maximum_time_inactivity, AppSettings.TIME_MAX_INACTITIVY))
                    return
                }

            } else { // For other fragments - set as active

                timeActive = AppSettings.TIME_MAX_INACTITIVY

            }

            if (!ble.sending && sendFeedback ) {

                // Send feedback periodically

                timeFeedback++

                if (timeFeedback == AppSettings.TIME_SEND_FEEDBACK) {

                    bleSendFeedback()
                    timeFeedback = 0

                }
            }

            // Is the BT Icone active?

            if (bleStatusActive > 0) {

                bleStatusActive--

                if (bleStatusActive == 0) {

                    // Displays not active

                    bleShowIconActive(false)
                }
            }

            // Terminal BLE

            if (AppSettings.TERMINAL_BLE) {

                if (fragmentActual == "TerminalBLE") {

                    // Is to send repeated (only for echoes) ?

                    if (fragmentTerminalBLE.repeatSend) {

                        // Show a debug with the total per second

                        if (fragmentTerminalBLE.bleTotRepeatPSec > 0) {

                            bleAddDebug( 'O', getString(R.string.tble_repeats_p_sec) + "${fragmentTerminalBLE.bleTotRepeatPSec}", "", true)

                        } else { // Send again, if no responses received

                            fragmentTerminalBLE.send(true)

                        }

                        // Clear the total

                        fragmentTerminalBLE.bleTotRepeatPSec = 0

                    } else {


                    }
                }
            }

        } catch (e: Exception) {
            this.extShowException(e)
        }
    }

    // Try a new connection

    fun bleTryConnectAgain(scan: Boolean = false): Boolean {

        try {

            // Attempt

            ble.nrAttemptConnection = 1

            // Attempt the connection to the last one or make a search

            if (!scan && ble.lastAddrConnected != null && ble.nrErrorsConnection <= 2) { // Last or 2 attempts

                fragmentConnection.showConnecting(false)

                ble.connectLastDevice()

            } else { // Scan

                fragmentConnection.showConnecting(true)

                ble.scanDevice()
            }

            return true
        } catch (e: Exception) {
            this.extShowException(e)
            return false
        }
    }

    // Add Debug BLE

    fun bleAddDebug (type: Char, message: String, extra: String = "", forced: Boolean = false) {


        // App have a Terminal BLE (debug)

        if (!(AppSettings.TERMINAL_BLE && (bleDebugEnabled || forced))) { // App have a Terminal BLE (debug) and it is enabled ?
            return
        }

        // Add debug

        val bleDebug = BLEDebug()

        // Time

        bleDebug.time = FormatterUtil.timeNow()

        // Type

        bleDebug.type = type

        // Message

        bleDebug.message = message.replace("\r", "\\r").replace("\n", "\\n")

        // Extra

        bleDebug.extra = extra

        // Add it

        if (AppSettings.TERMINAL_BLE_ORDER_DESC) { // Top

            bleDebugs.add(0,bleDebug)

        } else { // Bottom

            bleDebugs.add(bleDebug)
        }

        // In Terminal BLE screen ?

        if (fragmentActual == "TerminalBLE") {

            // Not for repeated sends - due can crash the app - in this case use refresh button

            if (!fragmentTerminalBLE.repeatSend || forced) {

                // Update

                fragmentTerminalBLE.adapter.notifyDataSetChanged()

            }
        }
    }

    // Update last Debug BLE

    fun bleUpdateDebug (extra: String) {

        // App have a Terminal BLE (debug)

        if (!(AppSettings.TERMINAL_BLE && bleDebugEnabled)) { // App have a Terminal BLE (debug) and it is enabled ?
            return
        }

        // Update last debug

        if (bleDebugs.isEmpty()) {
            return
        }

        val pos: Int = (if (AppSettings.TERMINAL_BLE_ORDER_DESC) 0 else bleDebugs.size-1)

        val item = bleDebugs[pos]

        item.extra = extra

        // In Terminal BLE screen ?

        if (fragmentActual == "TerminalBLE") {

            // Update // TODO optimizer this

            fragmentTerminalBLE.adapter.notifyDataSetChanged()
        }
    }

    // Displays status message

    @JvmOverloads
    fun showMessageStatus(texto: String?, tempoMillisApagar: Int = 0) {

        if (textViewStatus != null) {

            runOnUiThread {
                try {
                    textViewStatus!!.text = texto
                } catch (e: Exception) {
                    this.extShowException(e)
                }
            }

            if (tempoMillisApagar > 0) {
                timerStatusHandler.removeCallbacksAndMessages(null)
                timerStatusHandler.postDelayed({ showMessageStatus("", 0) }, tempoMillisApagar.toLong())
            }
        }

    }

    // Title

    fun setTitle(titulo: String) {

        if (textViewTitle != null)
            textViewTitle!!.text = titulo
    }

}

///// Fim
