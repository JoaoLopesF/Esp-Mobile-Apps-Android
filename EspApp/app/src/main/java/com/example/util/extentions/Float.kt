/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Extensions - for Float
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.extentions

import java.math.BigDecimal
import java.util.*

// Round value

@JvmOverloads
fun Float.extRound(decimals: Int = 2): Float {

    var value = this

    var bigDecimal = BigDecimal(java.lang.Float.toString(value))
    bigDecimal = bigDecimal.setScale(decimals, BigDecimal.ROUND_HALF_UP)

    value = bigDecimal.toFloat()


    return value
}

// To string

fun Float.extToString(): String {

    // To string formatted to 2 decimals // TODO setting

    return String.format(Locale.getDefault(), "%.2f", this)
}

