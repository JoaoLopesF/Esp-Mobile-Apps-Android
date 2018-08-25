/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : AdapterListMainMenu - adapter to main menu list
 * Comments  : based on http://www.vogella.com/tutorials/AndroidListView/article.html
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.espapp.adapters

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.example.espapp.R
import com.example.espapp.activities.MainActivity
import com.example.espapp.models.app.MenuOption

class AdapterListMainMenu(private var mainActivity: MainActivity,
                          private var contexto: Context,
                          private var menuOptions: MutableList<MenuOption>) : ArrayAdapter<MenuOption>(contexto,
                                                                            R.layout.fragment_mainmenu_list, menuOptions) {

    // Events

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rowView: View

        if (menuOptions[position].enabled) {
            rowView = LayoutInflater.from(contexto).inflate(R.layout.fragment_mainmenu_list, null)
        } else {
            rowView = LayoutInflater.from(contexto).inflate(R.layout.fragment_mainmenu_list_disabled, null)
        }

        val textViewName = rowView.findViewById<View>(R.id.textViewMMenuName) as TextView
        val textViewDesc = rowView.findViewById<View>(R.id.textViewMMenuDesc) as TextView
        textViewName.text = menuOptions[position].name
        textViewDesc.text = menuOptions[position].description

        val drawableImagem = menuOptions[position].drawableImagem

        //Show image

        if (drawableImagem > 0) {

            try {
                val img = rowView.findViewById<View>(R.id.imageViewMMain) as ImageView
                img.setImageResource(drawableImagem)

                if (!menuOptions[position].enabled) {
                    // Transform to gray scale - based em http://stackoverflow.com/questions/8381514/android-converting-color-image-to-grayscale
                    val matrix = ColorMatrix()
                    matrix.setSaturation(0f)
                    val filter = ColorMatrixColorFilter(matrix)
                    img.colorFilter = filter
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        rowView.isEnabled = menuOptions[position].enabled

        return rowView
    }

}

//////// End
