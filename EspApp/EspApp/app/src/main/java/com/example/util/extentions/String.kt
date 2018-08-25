/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Extensions - for String
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.extentions

import android.os.Build
import android.text.Html
import android.text.Spanned

// Expand string (to debugs)

fun String.extExpandStr(): String {

    // Espande a string para mostrar os caracteres nao visiveis

    var str = this

    str = str.replace("\n", "\\n")
    str = str.replace("\r", "\\r")
    str = str.replace("\t", "\\t")
    str = str.replace("\u0000", "\\0")

    return str

}

// Is int ?

fun String?.extIsInt(): Boolean {

    return if (this != null) {

        try {
            Integer.parseInt(this)
        } catch (e: NumberFormatException) {
            return false
        }

        true

    } else {
        false
    }
}

// Convert to int

fun String?.extToInt(): Int {

    return if (this != null) {
        try {
            Integer.parseInt(this)
        } catch (e: Exception) {
             -1
        }

    } else {

        -1
    }
}

// Load HTML in string

fun String.extToSpanned(): Spanned {

    // Based em: https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        return Html.fromHtml(this)
    }
}

