/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : FragmentInfoESP32 - fragment of informations about ESP32 connected device
 * Comments  :
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.activities


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.espapp.R
import com.example.espapp.helpers.MessagesBLE
import com.example.util.logD

// Using "kotlin-android-extensions" - see in https://kotlinlang.org/docs/tutorials/android-plugin.html

import kotlinx.android.synthetic.main.fragment_info_esp32.*

class FragmentInfoESP32 : Fragment() {

    ///// MainActivity

    lateinit var mainActivity: MainActivity

    ///// Variables

    ///// Proprieties

    ///// Events

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_info_esp32, container, false)

        logD("")

        // Previous state saved, does nothing

        if (savedInstanceState != null) {
            return rootView
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views

        mainActivity.setTitle(getString(R.string.info_esp32_title))

        buttonInfoFreeMemRef.setOnClickListener {

            // Request info

            sendInfoMessage("${MessagesBLE.MESSAGE_INFO}FMEM")

        }

        buttonInfoVoltEsp32Ref.setOnClickListener {

            // Request info

            sendInfoMessage("${MessagesBLE.MESSAGE_INFO}VDD33")

        }

        buttonInfoVoltBatRef.setOnClickListener {

            // Request info

            if (mainActivity.deviceHaveBattery) {
                sendInfoMessage("${MessagesBLE.MESSAGE_ENERGY}")
            }
        }

        buttonInfoRefreshAll.setOnClickListener {

            // Request all
            // Note: Example for  send more than 1 message once time
            // Due BLE routines handle large message
            // Its is better than send 3 messages

            var messages:String = ""
            messages += "${MessagesBLE.MESSAGE_INFO}FMEM"
            messages += '\n'
            messages += "${MessagesBLE.MESSAGE_INFO}VDD33"
            messages += '\n'
            if (mainActivity.deviceHaveBattery) {
                messages += "${MessagesBLE.MESSAGE_ENERGY}"
                messages += '\n'
            }

            sendInfoMessage(messages)
        }

        // Request all informations (example how send BLE messages from another VC)
        // the true indicate to wait to response (timeout, if not receive nothing a time)

        sendInfoMessage("${MessagesBLE.MESSAGE_INFO}ALL")

    }

    override fun onDetach() {
        super.onDetach()

        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)

        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }

    }

    override fun onResume() {
        super.onResume()

        // Fragment actual

        mainActivity.fragmentActual = "InfoESP32"

    }

    override fun onPause() {
        super.onPause()

    }

    /////// Routines

    // Update energy info

    fun updateEnergyInfo() {

        if (!mainActivity.deviceHaveBattery || mainActivity.poweredExternal) {

            textViewInfoPowered.text = "External"

        } else {

            textViewInfoPowered.text = "Battery"

        }

        if (mainActivity.deviceHaveSenCharging) {

            textViewInfoCharging.text = if (mainActivity.chargingBattery) "Yes" else "No"

        } else {

            textViewInfoCharging.text = "?"

        }

        textViewInfoVoltBat.text = "${mainActivity.readADCBattery} (${mainActivity.voltageBattery}v)"

    }

    // Send BLE message for info

    fun sendInfoMessage (message: String) {

        mainActivity.bleSendMessage(message, true, getString(R.string.ext_info))
    }

    // Set informations about ESP32

    fun setInfoESP32(info: String, type: String) {

        when (type) {
            "INFO_ESP32"
            -> {
                editTextInfoEsp32.setText(info)
            }
            "VOLT_ESP32"
            -> {
                textViewInfoVoltEsp32.text = info
            }
            "FREE_MEM_ESP32"
            -> {
                textViewInfoFreeMem.text = info
            }
        }
    }
}

////// End
