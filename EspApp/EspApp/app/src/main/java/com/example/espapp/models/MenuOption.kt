/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Models - object models for app
 * Comments  : MenuOption - used in menus
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.models.app

import java.io.Serializable

class MenuOption : Serializable {

    lateinit var code: String
    lateinit var name: String
    lateinit var description: String

    var drawableImagem: Int = 0

    var enabled: Boolean = false

    companion object {
        const val serialVersionUID = 1L
    }

}
