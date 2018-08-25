/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : FragmentTerminalBLE - fragment to terminal BLE
 * Comments  :
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.activities

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.example.espapp.R
import com.example.espapp.adapters.AdapterRecViewTerminalBLE
import com.example.espapp.helpers.MessagesBLE
import com.example.util.extentions.extHideVirtualKeyboard
import com.example.util.extentions.extShowToast
import com.example.util.logActive

// Using "kotlin-android-extensions" - see in https://kotlinlang.org/docs/tutorials/android-plugin.html

import kotlinx.android.synthetic.main.fragment_terminal_ble.*


class FragmentTerminalBLE : Fragment() {

    // Main Activity

    lateinit var mainActivity: MainActivity

    ////// Variables

    var repeatSend: Boolean = false         // Repeated sends ?
        private set

    var bleTotRepeatPSec: Int = 0           // Total of repeat (send/receive echo) per second

    private var savedLogActive = logActive // Save it

    // Views

    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter: AdapterRecViewTerminalBLE
        private set

    // Events

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_terminal_ble, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Previous state saved, does nothing // TODO see

        if (savedInstanceState != null) {
            return
        }

        // Views

        mainActivity.setTitle(getString(R.string.terminal_ble_title))

        linearLayoutManager = LinearLayoutManager(this.context)

        recyclerViewTBLE.layoutManager = linearLayoutManager

        AdapterRecViewTerminalBLE.typeConnColor = textViewTBLETypeConn.currentTextColor
        AdapterRecViewTerminalBLE.typeDisconnColor = textViewTBLETypeDisconn.currentTextColor
        AdapterRecViewTerminalBLE.typeRecvColor = textViewTBLETypeRecv.currentTextColor
        AdapterRecViewTerminalBLE.typeSendColor = textViewTBLETypeSend.currentTextColor
        AdapterRecViewTerminalBLE.typeOtherColor = textViewTBLETypeOther.currentTextColor

        adapter = AdapterRecViewTerminalBLE(mainActivity.bleDebugs)
        recyclerViewTBLE.adapter = adapter

        setRecyclerViewScrollListener()

        editTextTBLESend.setOnEditorActionListener() { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){

                // Send the message

                send (false)

                // Hide keyboard

                mainActivity.extHideVirtualKeyboard()

                true
            } else {
                false
            }
        }

        buttonTBLESend.setOnClickListener {

            // Send

            send(false)
        }

        buttonTBLERepeat.setOnClickListener {

            // Send repeat (new send at each receive - only for echos (70:xxx))

            if (repeatSend) {

                // Stop this

                stopRepeat()

                mainActivity.extShowToast(getString(R.string.repeat_sends_is_stopped))

            } else {

                // Send and repeat

                send(true)
            }
        }

        // Default send text

        if (editTextTBLESend.text.isEmpty()) {
            editTextTBLESend.setText("${MessagesBLE.MESSAGE_ECHO}" + getString(R.string.echo_test))
        }

    }

    override fun onResume() {
        super.onResume()

        // Fragment actual

        mainActivity.fragmentActual = "TerminalBLE"

        // Reload data

        //reloadData()
    }

    override fun onPause() {
        super.onPause()

        // Stop repeats

        if (repeatSend) {
            stopRepeat()
        }
    }

    override fun onStop() {
        super.onStop()

        // Stop repeats

        if (repeatSend) {
            stopRepeat()
        }
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

    /////// Routines

    // Set scroll listener

    private fun setRecyclerViewScrollListener() {

        recyclerViewTBLE.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        })
    }

    // Send

    fun send (repeated: Boolean) {

        // Have content to send ?

        var content:String = editTextTBLESend.text.toString()

        if (content.isEmpty()) {

            mainActivity.extShowToast(getString(R.string.empty_data))
            return
        }


        // For repeat -> only type echo messages allowed

        if (repeated && !content.startsWith(MessagesBLE.MESSAGE_ECHO, 0)) {

            mainActivity.extShowToast(getString(R.string.for_repeats_only_echo_msgs) + "(${MessagesBLE.MESSAGE_ECHO})")
            return
        }

        // If is first time of repeat, append message to disable logging in device

        if (!repeatSend && repeated) {

            content += '\n'
            content += "${MessagesBLE.MESSAGE_LOGGING}N"
            content += '\n'

        }
        // Send it

        mainActivity.bleSendMessage(content, true, getString(R.string.ext_terminal))

        // Repeated

        if (repeated && !repeatSend) {

            startRepeats()
        }
    }

    // Start repeats

    private fun startRepeats() {

        repeatSend = true

        buttonTBLERepeat.text = getString(R.string.stop)

        mainActivity.extShowToast(getString(R.string.repeated_sends_now))

        editTextTBLESend.visibility = View.GONE
        buttonTBLESend.visibility = View.GONE

        // Deactivate debugs on App and ESP32 to better performance

        mainActivity.bleDebugEnabled = false  // Disable it, for send repeated

        savedLogActive = logActive // Save it

        logActive = false

//        // Send this with delay
//
//        val handler = Handler()
//        handler.postDelayed({
//
//            mainActivity.bleSendMessage("${MessagesBLE.MESSAGE_LOGGING}N", false,  getString(R.string.by_terminal))
//
//        }, 200)
    }

    // Stop repeats

    private fun stopRepeat() {

        buttonTBLERepeat.text = getString(R.string.repeat)

        editTextTBLESend.visibility = View.VISIBLE
        buttonTBLESend.visibility = View.VISIBLE

        // Restore debugs

        if (!mainActivity.bleDebugEnabled) {

            mainActivity.bleDebugEnabled = true // Enable it

            logActive = savedLogActive // Enable it

            // Send this with delay

            val handler = Handler()
            handler.postDelayed({

                mainActivity.bleSendMessage("${MessagesBLE.MESSAGE_LOGGING}R", false, getString(R.string.ext_terminal)) // Restore it

            }, 500)
        }

        // Indicate to no repeat

        repeatSend = false
        bleTotRepeatPSec = 0

    }
}

///// End
