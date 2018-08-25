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
import com.example.espapp.models.app.MenuOption
import com.example.util.extentions.UI.extSetHeightBasedOnChildren
import com.example.util.extentions.extShowToast

class FragmentMainMenu : Fragment() {

    // MainActivity

    lateinit var mainActivity: MainActivity

    // View

    private var listViewMMain: ListView? = null

    // Variables

    private lateinit var menuOptions: Array<MenuOption>
        private set

    // Events

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_mainmenu, container, false)

        // Previous state saved, does nothing

        if (savedInstanceState != null) {
            return rootView
        }

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

    private fun processOption(position: Int, longo: Boolean) {

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

        menuOptions = Array(3) { MenuOption() }

        var pos = 0

        while (pos < menuOptions.size) {
            menuOptions[pos] = MenuOption()
            pos++
        }

        pos = 0

        // Informations about ESP32

        this.menuOptions[pos].code = "INFO"
        menuOptions[pos].name = getString(R.string.mainmenu_info_name)
        menuOptions[pos].description = getString(R.string.mainmenu_info_desc)
        menuOptions[pos].drawableImagem = R.drawable.info
        menuOptions[pos].enabled = true

        // Terminal BLE

        pos++

        menuOptions[pos].code = "TERMINAL"
        menuOptions[pos].name = getString(R.string.mainmenu_term_name)
        menuOptions[pos].description = getString(R.string.mainmenu_term_desc)
        menuOptions[pos].drawableImagem = R.drawable.terminal
        menuOptions[pos].enabled = true

        // AppSettings

        pos++

        menuOptions[pos].code = "SETTINGS"
        menuOptions[pos].name = getString(R.string.mainmenu_sett_name)
        menuOptions[pos].description = getString(R.string.mainmenu_sett_desc)
        menuOptions[pos].drawableImagem = R.drawable.settings
        menuOptions[pos].enabled = false

    }
}

///// End
