/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : FragmentMainMenu - fragment to main menu
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
import android.widget.AdapterView
import android.widget.ListView

import com.example.espapp.R
import com.example.espapp.adapters.AdapterListMainMenu
import com.example.espapp.helpers.AppSettings
import com.example.espapp.models.app.MenuOption
import com.example.util.extentions.UI.extSetHeightBasedOnChildren
import com.example.util.extentions.extShowToast
import com.example.util.logV

class FragmentMainMenu : Fragment() {

    // MainActivity

    lateinit var mainActivity: MainActivity

    // View

    private var listViewMMain: ListView? = null

    // Variables

    private lateinit var menuOptions:  MutableList<MenuOption>                 

    // Events

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        logV("")

        val rootView = inflater.inflate(R.layout.fragment_mainmenu, container, false)

//        // Previous state saved, if you need this
//
//        if (savedInstanceState != null) {
//        }

        // Views

        mainActivity.setTitle(getString(R.string.main_menu_title))

        listViewMMain = rootView.findViewById<View>(R.id.listViewMMain) as ListView

        // Show main menu

        showMainMenu()

        return rootView
    }

    override fun onResume() {
        super.onResume()

        // Fragment actual

        mainActivity.fragmentActual = "MainMenu"

    }

    override fun onStop() {
        super.onStop()

        logV("")
    }

    override fun onDetach() {
        super.onDetach()

        logV("")

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

    // Show the main menu

    fun showMainMenu() {

        // Process data

        processData()

        // Adapter

        val adapter = AdapterListMainMenu(mainActivity, mainActivity.applicationContext, menuOptions)

        listViewMMain!!.adapter = adapter

        // Events

        listViewMMain!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Process option

            processOption(position, false)
        }

        listViewMMain!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            // Process option

            processOption(position, true)
            true
        }

        listViewMMain!!.extSetHeightBasedOnChildren()

    }

    // Process option selected

    private fun processOption(position: Int, long: Boolean) {

        val menuOption = this.menuOptions[position]
        var code = menuOption.code

        // Process code

        when (code) {

            "INFO" -> { // Informations abount ESP32

                if (!mainActivity.modeDemo) {

                    showFragmentInfoESP32()

                } else {

                    mainActivity.extShowToast(getString(R.string.not_allowed_in_demo))
                }

            }

            "TERMINAL" -> { // Terminal BLE

                if (!mainActivity.modeDemo) {

                    showFragmentTerminalBLE()

                } else {

                    mainActivity.extShowToast(getString(R.string.not_allowed_in_demo))
                }

            }

            "SETTINGS" -> { // Settings

//                exibirFragmentHSettings()

            }

        }
    }

    // Show fragment of informations about the ESP32

    private fun showFragmentInfoESP32() {

        val fragmentTransaction = mainActivity.supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment, mainActivity.fragmentInfoESP32)

        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()

    }

    // Show fragment of terminal BLE

    private fun showFragmentTerminalBLE() {

        val fragmentTransaction = mainActivity.supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment, mainActivity.fragmentTerminalBLE)

        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()

    }

    // Process data to show in menu

    private fun processData() {

        menuOptions = mutableListOf()
        
        // Informations about ESP32

        if (AppSettings.ESP32_INFORMATIONS) {

            val optInfo = MenuOption()
            optInfo.code = "INFO"
            optInfo.name = getString(R.string.mainmenu_info_name)
            optInfo.description = getString(R.string.mainmenu_info_desc)
            optInfo.drawableImagem = R.drawable.info
            optInfo.enabled = true
            
            this.menuOptions.add(optInfo)
        }

        // Terminal BLE

        if (AppSettings.TERMINAL_BLE) {

            val optTerminal = MenuOption()

            optTerminal.code = "TERMINAL"
            optTerminal.name = getString(R.string.mainmenu_term_name)
            optTerminal.description = getString(R.string.mainmenu_term_desc)
            optTerminal.drawableImagem = R.drawable.terminal
            optTerminal.enabled = true

            this.menuOptions.add(optTerminal)
        }

        // AppSettings - remove it if you not need this

        val optSettings = MenuOption()

        optSettings.code = "SETTINGS"
        optSettings.name = getString(R.string.mainmenu_sett_name)
        optSettings.description = getString(R.string.mainmenu_sett_desc)
        optSettings.drawableImagem = R.drawable.settings
        optSettings.enabled = false

        this.menuOptions.add(optSettings)

    }
}

///// End
