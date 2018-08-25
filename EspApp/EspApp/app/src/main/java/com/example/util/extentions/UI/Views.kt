/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Extensions - for View
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.extentions.UI

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.util.logE
import java.io.File
import java.io.FileInputStream

// Extensions to ImageView // TODO optimize this

@Throws(Exception::class)
fun ImageView?.extloadFile(path: String) {
    this.extloadFile(File(path))
}

@Throws(Exception::class)
@JvmOverloads
fun ImageView?.extloadFile(file: File, scale: Boolean = false) {

    // Load a image

    try {

        if (file.exists()) {

            val streamIn = FileInputStream(file)

            var bitmap = BitmapFactory.decodeStream(streamIn) //This gets the image

            if (scale) { // Scale image
                val maximo = 2048
                val nh = (bitmap.height * (maximo * 1.0 / bitmap.width)).toInt()
                bitmap = Bitmap.createScaledBitmap(bitmap, maximo, nh, true)
            }

            this?.setImageBitmap(bitmap)

            streamIn.close()

        } else {

            throw Exception("File not exists : " + file.path)
        }

    } catch (e: Exception) {
//        activity!!.extShowException(e)
        logE("extloadFile")
        e.printStackTrace()
    }

}

// Extension for View

fun View?.extSetEfectButton() {

    // Based em: https://stackoverflow.com/questions/7175873/click-effect-on-button-in-android

    this?.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.background.setColorFilter(-0x1f0b8adf, PorterDuff.Mode.SRC_ATOP)
                v.invalidate()
            }
            MotionEvent.ACTION_UP,
                // Your action here on button click
            MotionEvent.ACTION_CANCEL -> {
                v.background.clearColorFilter()
                v.invalidate()
            }
        }
        false
    }
}

// Extension for ViewGroup

fun ViewGroup.extInflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

////// End