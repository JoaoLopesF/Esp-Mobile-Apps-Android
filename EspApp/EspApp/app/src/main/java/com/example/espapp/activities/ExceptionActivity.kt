/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : FragmentException - to show exceptions
 * Comments  :
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 *
 **/package com.example.espapp.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.espapp.R
import com.example.util.Util

class ExceptionActivity : Activity() {

    private var buttonEncerrar: Button? = null
    private var textViewmessage: TextView? = null
    private var textViewExcecao: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(null)

        setContentView(R.layout.activity_exception)

        textViewmessage = findViewById<View>(R.id.textViewMessage) as TextView

        textViewmessage!!.text = intent.getStringExtra("Message")

        textViewExcecao = findViewById<View>(R.id.textViewException) as TextView

        textViewExcecao!!.text = intent.getStringExtra("Exception")

        buttonEncerrar = findViewById<View>(R.id.buttonFinish) as Button

        buttonEncerrar!!.setOnClickListener {
            // Abort

            Util.abortActivity(this)
        }

    }

}
