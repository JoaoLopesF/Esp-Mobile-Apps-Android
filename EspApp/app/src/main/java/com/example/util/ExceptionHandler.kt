/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : ExceptionHandler
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/


package com.example.util

// Based em https://github.com/hardik-trivedi/ForceClose

import android.app.Activity
import com.example.util.extentions.extShowException

class ExceptionHandler (private val contexto: Activity) : Thread.UncaughtExceptionHandler {

    // Event of exception

    override fun uncaughtException(thread: Thread?, exception: Throwable?) {

        logE("")

        contexto.extShowException(null, exception as Exception)
    }

}
