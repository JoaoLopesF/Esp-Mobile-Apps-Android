/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Util - general utilities
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.example.util.extentions.*
import android.content.IntentFilter
import android.view.inputmethod.InputMethodManager


// TODO Migrate whatever you give to extensions

@Suppress("NOTHING_TO_INLINE")
object Util {

    //**** Utils

    // Causes execution error

    inline fun causeError (message: String) {


        throw RuntimeException (message)

    }

    // Finalize the app

    fun finalizeApp() {

        logI("Finalizing")

        try {
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        System.exit(0)

    }

    // Aborts the application (case of serious error)

    fun abortActivity(activity: Activity?) {

        logI ("Aborting activity")
        
        // Finalize activity

        if (activity != null) {
            activity.finish()
        }

        // Finalize the app

        finalizeApp()
    }

    //// Hardware

    // Emulator ?

    val isEmulator: Boolean
        get() = Build.MODEL.contains("Android SDK built for x86")

    // GPS activated ? // see in https://stackoverflow.com/questions/44024009/how-to-check-if-gps-i-turned-on-or-not-programmatically-in-android-version-4-4-a
    
    fun isLocationEnabled(context: Context): Boolean {
        
        var locationMode: Int
        var locationProviders: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)

            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF

        } else {
            // TODO deprecaded
            locationProviders = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            return !TextUtils.isEmpty(locationProviders)
        }
    }

    // Is USB connected
    
    fun isUSBConnected(context: Context): Boolean {

        if (isEmulator) {
            return true
        }

        // Verify it

        val intent = context.registerReceiver(null, IntentFilter("android.hardware.usb.action.USB_STATE"))
        return intent.getExtras().getBoolean("connected")
    }

    ////// Inlines to Kotlin extensions

    inline fun isInt(valor: String): Boolean = valor.extIsInt()
    inline fun convStrInt(str: String): Int = str.extToInt()
    inline fun convIntStr(value: Int): String? = value.toString()
    inline fun convFloatStr(value: Float): String = value.extToString()
    inline fun round(value: Float, decimais: Int = 2): Float = value.extRound(decimais)

}


////// End