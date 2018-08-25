/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Extensions - for ListView
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.extentions.UI

import android.widget.ListView

fun ListView.extSetHeightBasedOnChildren() {

    // View the list of views according to the text of this

    try {

        val listAdapter = this.adapter ?: return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, this)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        val params = this.layoutParams
        params.height = totalHeight + this.dividerHeight * (listAdapter.count - 1)
        this.layoutParams = params
        this.requestLayout()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
