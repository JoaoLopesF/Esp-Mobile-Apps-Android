/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Preferences - for Android preferences
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.util.Util.causeError

/////// Preferences

object Preferences {

    private lateinit var sharedPreferences: SharedPreferences
    private var isInitialized: Boolean = false

    /////// Methods

    // Initialize

    fun initialize(context: Context) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        isInitialized = true
    }

    // Read preference

    fun read(name: String, default: String? = null): String? {

        if (!isInitialized)
            causeError("Preferences not initialized")

        return sharedPreferences.getString(name, default)
    }

    fun read(name: String, default: Char): Char {

        if (!isInitialized)
            causeError("Preferences not initialized")

        val aux = sharedPreferences.getString(name, "" + default)
        return aux!![0]
    }

    fun read(name: String, default: Boolean): Boolean {


        if (!isInitialized)
            causeError("Preferences not initialized")

        return sharedPreferences.getBoolean(name, default)
    }

    fun read(name: String, default: Int): Int {

        if (!isInitialized)
            causeError("Preferences not initialized")

        return sharedPreferences.getInt(name, default)
    }

    // Save preferences
    
    fun save(name: String, value: String) {

        if (!isInitialized)
            causeError("Preferences not initialized")

        val editor = sharedPreferences.edit()
        editor.putString(name, value)
        editor.apply() //commit();
    }

    fun save(name: String, value: Char) {
        save(name, "$value")
    }

    fun save(name: String, value: Boolean) {

        if (!isInitialized)
            causeError("Preferences not initialized")

        val editor = sharedPreferences.edit()
        editor.putBoolean(name, value)
        editor.apply()
    }

    fun save(name: String, value: Int) {

        if (!isInitialized)
            causeError("Preferences not initialized")

        val editor = sharedPreferences.edit()
        editor.putInt(name, value)
        editor.apply()
    }
}

////// End