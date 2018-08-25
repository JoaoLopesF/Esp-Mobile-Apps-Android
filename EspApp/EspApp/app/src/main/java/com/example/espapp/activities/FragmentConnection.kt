/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : FragmentConnection - to treat BLE connections/disconnections
 * Comments  :
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.activities


import android.graphics.drawable.Drawable
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.example.espapp.R
import com.example.util.logD

class FragmentConnection : Fragment() {

    // Mainactivity

    lateinit var mainActivity: MainActivity

    // Views

    private var layoutConnecting: LinearLayout? = null
    private var layoutError: LinearLayout? = null
    private var textViewAction: TextView? = null
    private var textViewError: TextView? = null
    private var buttonTryAgain: Button? = null
    private var imageViewApp: ImageView? = null
    private var imagemConnecting: Drawable? = null
    private var imagemDisconnected: Drawable? = null

    private var messageError: String? = null
    private var showTryAgain: Boolean = false
    private var scanning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_connection, container, false)

        logD("")

        // Previous state saved, does nothing

        if (savedInstanceState != null) {
            return rootView
        }

        // Views

        imageViewApp = rootView.findViewById<View>(R.id.imageViewConnApp) as ImageView
        layoutConnecting = rootView.findViewById<View>(R.id.layoutConnConnecting) as LinearLayout
        layoutError = rootView.findViewById<View>(R.id.layoutConnConnectionError) as LinearLayout

        textViewAction = rootView.findViewById<View>(R.id.textViewConnAction) as TextView
        textViewError = rootView.findViewById<View>(R.id.textViewConnError) as TextView

        buttonTryAgain = rootView.findViewById<View>(R.id.buttonConnTryAgain) as Button
        buttonTryAgain!!.setOnClickListener { mainActivity.bleTryConnectAgain() }
        buttonTryAgain!!.setOnLongClickListener { mainActivity.bleTryConnectAgain(true) }

        imagemConnecting = ContextCompat.getDrawable(context!!, R.drawable.app)
        imagemDisconnected = ContextCompat.getDrawable(context!!, R.drawable.app_not_connected)

        // Displays connection screen or connection error

        if (messageError == null)
            showConnecting(scanning)
        else
            showConnectionError(messageError)

        return rootView
    }

    override fun onResume() {
        super.onResume()

        mainActivity.fragmentActual = "Connection"

    }

    //    @Override
    //    public void onAttach(Context context) {
    //        super.onAttach(getContext());
    //    }

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

    fun showConnecting(scanning: Boolean) {

        // Exibe tela de conexao

        this.scanning = scanning

        if (layoutError == null || layoutConnecting == null)
            return

//        getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//

        layoutError!!.visibility = View.GONE

        textViewAction!!.text = getString(if (scanning)
            R.string.bt_scanning
        else
            R.string.bt_connecting)
        textViewError!!.text = ""

        imageViewApp!!.setImageDrawable(imagemConnecting)
        layoutConnecting!!.visibility = View.VISIBLE

//            }
//        });

        // Control variables

        messageError = null
        showTryAgain = true

    }

    // There was a connection error?

    fun showConnectionError(messageError: String?) {

        this.messageError = messageError

        if (messageError != null) {

            if (layoutError != null) {

                layoutError!!.layoutParams.height = layoutConnecting!!.layoutParams.height

                imageViewApp!!.setImageDrawable(imagemDisconnected)
                layoutError!!.visibility = View.VISIBLE

                if (textViewError != null)
                    textViewError!!.text = messageError

                if (layoutConnecting != null)
                    layoutConnecting!!.visibility = View.GONE

            }
        }
    }

    // Displays the try again button

    fun showButtonTryAgain(show: Boolean) {

        if (layoutError != null) {

            if (buttonTryAgain != null)
                buttonTryAgain!!.visibility = if (show) View.VISIBLE else View.GONE

        }
    }
}

///////// End
