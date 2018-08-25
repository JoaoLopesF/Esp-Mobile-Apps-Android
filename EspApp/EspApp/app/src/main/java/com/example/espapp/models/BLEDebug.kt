/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Models - object models for app
 * Comments  : BLEDebug - messages bleDebug
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.models

class BLEDebug  {

    var time: String = ""      // Time of debug
    var type: Char = ' '       // Type of debug: C=connection/D-disconnection/F-Find/P-Problem/R-Receive/S-Send
    var message: String = ""   // Message
    var extra: String = ""     // Message extra (for example: type of message)

}


