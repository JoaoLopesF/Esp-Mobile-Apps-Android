/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Helpers - helpers - static objects for the app
 * Comments  : MessagesBLE - static class helper to messages
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.helpers

import com.example.util.FormatterUtil

object MessagesBLE {

    /**
     * BLE text messages of this app
     * -----------------------------
     * Format: nn:payload
     * (where nn is code of message and payload is content, can be delimited too)
     * -----------------------------
     * Messages codes:
     * 01 Initial
     * 10 Energy status(External or Battery?)
     * 11 Informations about ESP32 device
     * 70 Echo bleDebug
     * 80 Feedback
     * 81 Logging
     * 98 Restart (reset the ESP32)
     * 99 Standby (enter in deep sleep)
     *
     * // TODO: see it! please remove that you not use and keep it updated
     **/

    // Messages code

    const val CODE_OK = 0
    const val CODE_INITIAL = 1
    const val CODE_ENERGY = 10
    const val CODE_INFO = 11
    const val CODE_ECHO = 70
    const val CODE_LOGGING = 71
    const val CODE_FEEDBACK = 80
    const val CODE_RESTART = 98
    const val CODE_STANDBY = 99

    // Messages start text (with code)

    val MESSAGE_OK = "${FormatterUtil.number(CODE_OK, "00")}:"
    val MESSAGE_INITIAL = "${FormatterUtil.number(CODE_INITIAL, "00")}:"
    val MESSAGE_INFO = "${FormatterUtil.number(CODE_INFO, "00")}:"
    val MESSAGE_ENERGY = "${FormatterUtil.number(CODE_ENERGY, "00")}:"
    val MESSAGE_ECHO = "${FormatterUtil.number(CODE_ECHO, "00")}:"
    val MESSAGE_LOGGING = "${FormatterUtil.number(CODE_LOGGING, "00")}:"
    val MESSAGE_FEEDBACK = "${FormatterUtil.number(CODE_FEEDBACK, "00")}:"
    val MESSAGE_RESTART = "${FormatterUtil.number(CODE_RESTART, "00")}:"
    val MESSAGE_STANDBY = "${FormatterUtil.number(CODE_STANDBY, "00")}:"

    val MESSAGE_ERROR = "-1:"
}