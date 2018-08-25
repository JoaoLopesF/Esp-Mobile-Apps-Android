/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : AdapterListMainMenu - adapter to main menu list
 * Comments  : based on https://www.raywenderlich.com/367-android-recyclerview-tutorial-with-kotlin
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import android.view.View
import com.example.espapp.R
import com.example.espapp.models.BLEDebug
import com.example.util.extentions.UI.extInflate
import com.example.util.logV

// Kotlin Android plugin

import kotlinx.android.synthetic.main.fragment_terminal_ble_item.view.*

// Adapter class

class AdapterRecViewTerminalBLE(private val bleDebugs: MutableList<BLEDebug>) : RecyclerView.Adapter<AdapterRecViewTerminalBLE.BLEDebugHolder>() {

    // Companion static

    companion object {

        var typeConnColor: Int = 0
        var typeDisconnColor: Int = 0
        var typeRecvColor: Int = 0
        var typeSendColor: Int = 0
        var typeOtherColor: Int = 0
    }

    // Proprieties/Events

    override fun getItemCount() = bleDebugs.size

    override fun onBindViewHolder(holder: AdapterRecViewTerminalBLE.BLEDebugHolder, position: Int) {
        val item = bleDebugs[position]
//        logV("****")
        holder.bindBLEDebug(item)  }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRecViewTerminalBLE.BLEDebugHolder {
        val inflatedView = parent.extInflate(R.layout.fragment_terminal_ble_item, false)
        return BLEDebugHolder(inflatedView)
    }

    // Holder class

    class BLEDebugHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var bleDebug: BLEDebug? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
//            val context = itemView.context
        }

        fun bindBLEDebug(bleDebug: BLEDebug) {

//            logV("***")

            this.bleDebug = bleDebug

            var debug: String =
                    if (bleDebug.type == 'R' || bleDebug.type == 'S') {
                        "${bleDebug.time}: ${bleDebug.type}[${bleDebug.message.length}]: ${bleDebug.message} [${bleDebug.extra}]"
                    } else {
                        "${bleDebug.time}: ${bleDebug.type}: ${bleDebug.message} ${bleDebug.extra}"
                    }

            view.textViewTBLEMessage.text = debug

            when (bleDebug.type) {
                'C' ->
                    view.textViewTBLEMessage.setTextColor(typeConnColor)    // Connection
                'D' ->
                    view.textViewTBLEMessage.setTextColor(typeDisconnColor) // Disconnection
                'R' ->
                    view.textViewTBLEMessage.setTextColor(typeRecvColor)    // Receive
                'S' ->
                    view.textViewTBLEMessage.setTextColor(typeSendColor)    // Send
                'O' ->
                    view.textViewTBLEMessage.setTextColor(typeOtherColor)   // Others
                else ->
                    view.textViewTBLEMessage.setTextColor(Color.WHITE)      // ?
            }
        }
    }
}

///// End