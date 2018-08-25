/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : BLE - Utilities to BLE
 * Comments  : BLEHandler - Handler to BLE events
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/


package com.example.util.bluetooth

interface BLEHandler {
    fun onConnect()
    fun onAbortConnection(message: String)
    fun onConnecting()
    fun onConnected()
    fun onTryConnectAgain()
    fun onReceiveLine(linha: String)
}