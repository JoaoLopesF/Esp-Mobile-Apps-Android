/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : FormatterUtil - for format contents
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

//TODO convert all to extensions ?

object FormatterUtil {

    var formatDateLocale: String = "dd-MM-yy"
    var formatDateTimeLocale: String = "dd-MM-yy hh:mm:ss aa"
    var formatTimeLocale: String = "hh:mm:ss aa"

    // Format numbers

    fun number(number: Int, format: String): String {
        return number(number.toLong(), format)
    }

    fun number(number: Long, format: String): String {

        val formatoDecimal = DecimalFormat(format)
        return formatoDecimal.format(number)

    }

    // Format dates

    fun dateNow(): String {

        val date = Calendar.getInstance().time

        return dateTime(date)
    }

    fun dateTime(date: Date): String {

        val formatter = SimpleDateFormat(formatDateTimeLocale, java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun timeNow(): String {

        val date = Calendar.getInstance().time
        return FormatterUtil.time(date)
    }

    fun time(date: Date): String {

        val formatter = SimpleDateFormat(formatTimeLocale, java.util.Locale.getDefault())
        return formatter.format(date)
    }

}
