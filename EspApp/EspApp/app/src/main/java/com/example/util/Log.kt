/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Log - for Android logging
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/


@file:Suppress("NOTHING_TO_INLINE")

package com.example.util

import android.util.Log

////// Debug logging

var logTag: String = "<tag>" // Before it is set

var logActive = false // Active ?

////// Inline log routines

// Log - verbose

inline fun logV(message: String = "") {

    if (logActive) {
//        Log.v(logTag, message)
        val trace = "${Throwable().stackTrace[0].className.substringAfterLast(".")}.${Throwable().stackTrace[0].methodName}"
        Log.v(logTag, "$trace : $message")
    }

}

// Log - bleDebug

inline fun logD(message: String = "") {

    if (logActive) {
//        Log.d(logTag, message)
        val trace = "${Throwable().stackTrace[0].className.substringAfterLast(".")}.${Throwable().stackTrace[0].methodName}"
        Log.d(logTag, "$trace : $message")
    }

}

// Log - info

inline fun logI(message: String = "") {

    if (logActive) {
//        Log.i(logTag, message)
        val trace = "${Throwable().stackTrace[0].className.substringAfterLast(".")}.${Throwable().stackTrace[0].methodName}"
        Log.i(logTag, "$trace : $message")
    }

}

// Log - warning

inline fun logW(message: String = "") {

    if (logActive) {
//        Log.w(logTag, message)
        val trace = "${Throwable().stackTrace[0].className.substringAfterLast(".")}.${Throwable().stackTrace[0].methodName}"
        Log.w(logTag, "$trace : $message")
    }

}

// Log - error

inline fun logE(message: String) {

//    Log.e(logTag, message)
    val trace = "${Throwable().stackTrace[0].className.substringAfterLast(".")}.${Throwable().stackTrace[0].methodName}"
    Log.e(logTag, "$trace : $message")
}

inline fun logE(message: String, e: Exception) {

//    Log.e(logTag, message, e)
    val trace = "${e.stackTrace[0].className.substringAfterLast(".")}.${e.stackTrace[0].methodName}"
    Log.e(logTag, "$trace : $message", e)
}
