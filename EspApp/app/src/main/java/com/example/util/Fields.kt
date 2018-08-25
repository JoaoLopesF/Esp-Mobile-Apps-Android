/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Fields - to extract fields on delimited text (as messages BLE)
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util

class Fields (content: String, delim:String =  ":")  {

    // Variables

    var fields: Array<String>
    var lastField: Int = 0

    // Init

    init {

        // Split a string in a array

        fields = content.split(delim.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        lastField = 0

    }

    // Get a field - string

    fun getField (field:Int): String {

        var ret: String = ""

        if (field >= 1 && field <= fields.size) {
            ret = fields[field-1]
            lastField = field
        }
        return ret

    }

    // Get a field - integer

    fun getFieldInt (field:Int): Int {

        var ret: Int = 0

        val aux:String = getField(field)

        if (aux != "") {
            ret = Util.convStrInt(aux)
        }

        return ret
    }

    // Get a total of fields processed

    fun getTotalFields(): Int {

        return fields.size
    }

    // Get a next field

    fun getNextField(): String {

        lastField++

        return getField(lastField)
    }

    // Get a next field - int

    fun getNextFieldInt(): Int {

        lastField ++

        return getFieldInt(lastField)
    }

}
