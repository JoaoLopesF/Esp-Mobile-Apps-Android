/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Helpers - helpers - static objects for the app
 * Comments  : Settings - for settings
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.helpers

import com.example.espapp.BuildConfig
import com.example.util.Preferences

object AppSettings {

    ////// Settings of app

    //// Static

    // -- BLE

    const val BT_NAME_DEVICE = "Esp32_Device"       // Device name (start)

    const val BT_TIMEOUT = 3                        // Timeout

    // - Time & timeouts (in seconds)

    const val TIME_SEND_FEEDBACK  = 30              // Send feedbacks at this interval - put 0 to disable this

    const val TIME_MAX_INACTITIVY = 300             // Maximum time of inactivity

    // - Terminal BLE

    // Enable terminal BLE ? (by default only for DEBUG) (put false to disable it)

    val TERMINAL_BLE: Boolean = BuildConfig.DEBUG

    // Order descend ? (last show first ?)

    const val TERMINAL_BLE_ORDER_DESC: Boolean = true

    // - ESP32 Informations

    // Enable ESP32 Informations ? (by default only for DEBUG) (put false to disable it)

    val ESP32_INFORMATIONS: Boolean = BuildConfig.DEBUG

    // - Turn off - send a message to ESP32 device enter in deep sleep on exit of this app ?
    // (if the device not have standby function (deep sleep), it will restarted)

    const val TURN_OFF_DEVICE_ON_EXIT: Boolean = true

    ///// Variables -> preferences (can be changed)

    var modePowerSave = true                        // Mode power save ?

    ///// Initialization

    init {

        // Read preferences

        read()
    }

    ///// Methods

    // Read preferences saved

    fun read() {

        modePowerSave =  Preferences.read("AppSetModePowerSave", true)

    }

    // Save references

    fun save() {

        // Mode Power save ?

        Preferences.save("AppSetModePowerSave", modePowerSave)

    }
}

////// End