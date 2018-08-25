/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Extensions - for Activity
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.extentions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Looper
import android.support.annotation.StringRes
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.espapp.R


// Alerts // TODO: criar mais opcoes

var alertDialogExtActivity: AlertDialog? = null // To only allow an active alert

fun Activity?.extAlert(message: String, callbackOK: (() -> Unit)? = null) {

    // Show alert

    if (this == null) {
        return
    }

    if (alertDialogExtActivity != null) {
        alertDialogExtActivity!!.cancel()
        alertDialogExtActivity!!.hide()
    }

    this.runOnUiThread {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    if (callbackOK != null) {
                        callbackOK()
                    }
                }
        alertDialogExtActivity = alertDialogBuilder.create()
        alertDialogExtActivity!!.show()
    }

}

fun Activity?.extConfirm(message: String, callbackYes: (() -> Unit)? = null, callbackNo: (() -> Unit)? = null) {

    // Show alert

    if (this == null) {
        return
    }

    if (alertDialogExtActivity != null) {
        alertDialogExtActivity!!.cancel()
        alertDialogExtActivity!!.hide()
    }

    this.runOnUiThread {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    if (callbackYes != null) {
                        callbackYes()
                    }
                }
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                    if (callbackNo != null) {
                        callbackNo()
                    } else {
                        alertDialogExtActivity!!.hide()
                    }
                }
        alertDialogExtActivity = alertDialogBuilder.create()
        alertDialogExtActivity!!.show()
    }

}

// Toast

fun Activity?.extShowToast(message: CharSequence, longa: Boolean = false)  {

    // Displays the toast for a message

    if (this == null) {
        return
    }

    // Check thread

    if (Looper.myLooper() == Looper.getMainLooper()) { // Must be in the main UI

        val duracao: Int = if (longa) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(this.applicationContext, message, duracao).apply { show() }

    } else { // Not on the UI thread

        this.runOnUiThread {

            val duracao: Int = if (longa) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            Toast.makeText(this.applicationContext, message, duracao).apply { show() }
        }
    }
}

fun Activity?.extShowToast(@StringRes resId: Int, longa: Boolean = false) {

    // Displays the toast for a message by resource

    if (this == null) {
        return
    }

    // Check thread

    if (Looper.myLooper() == Looper.getMainLooper()) { // Must be in the main UI

        val duracao: Int = if (longa) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(this.applicationContext, resId, duracao).apply { show() }

    } else { // Not on the UI thread

        this.runOnUiThread {

            val duracao: Int = if (longa) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            Toast.makeText(this.applicationContext, resId, duracao).apply { show() }
        }
    }
}

// hide virtual keyboard - based on https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard

fun Activity?.extHideVirtualKeyboard() {

    val imm = this!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = this!!.getCurrentFocus()
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
}