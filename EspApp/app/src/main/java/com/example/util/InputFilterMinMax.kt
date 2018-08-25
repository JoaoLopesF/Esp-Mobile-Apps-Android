/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : InputFilterMinMax - for input filters
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax : InputFilter {

    private var min: Int = 0
    private var max: Int = 0

    constructor(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    constructor(min: String, max: String) {
        this.min = Integer.parseInt(min)
        this.max = Integer.parseInt(max)
    }

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = Integer.parseInt(dest.toString() + source.toString())
            if (isInRange(min, max, input))
                return null
        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c >= a && c <= b else c >= b && c <= a
    }
}