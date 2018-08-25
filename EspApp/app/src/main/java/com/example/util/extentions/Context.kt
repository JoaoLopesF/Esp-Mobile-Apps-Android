/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : Extensions - for Context
 * Versions  :
 * -------  --------    -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.util.extentions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import com.example.espapp.activities.ExceptionActivity
import com.example.util.logE
import com.example.util.logTag
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

// Extentions fro context

// Show exceptiop

fun Context?.extShowException(exception: Exception? = null) {
    this.extShowException(null, exception)
}

fun Context?.extShowException(message: String?, exception: Exception? = null) {

    // TODO: pass a reference

    // Treats unhandled exceptions and terminates application

    if (this == null) {
        return
    }

    // TODO: internationalization ?

    try {

        logE("message -> $message exception -> $exception")
        if (exception != null) {
            exception.printStackTrace()
        }

        Thread.sleep(10000)

        val errorReport = StringBuilder()
        errorReport.append("**************** Exception *****************")

        val sdfDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val now = Date()
        val dataHora = sdfDate.format(now)
        errorReport.append("\nDate: ").append(dataHora)

        if (message != null) {
            errorReport.append("\nMessage: ").append(message)

        }
        if (exception != null) {
            errorReport.append("\nException: ").append(exception.message)

            errorReport.append("\n************ Exception cause ************\n")
            val stackTrace = StringWriter()
            exception.printStackTrace(PrintWriter(stackTrace))
            errorReport.append(stackTrace.toString())
        }
        errorReport.append("\n**** Informations about cellphone/tablet *****\n")
        errorReport.append("Brand: ")
        errorReport.append(Build.BRAND)
        val LINE_SEPARATOR = "\n"
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Device: ")
        errorReport.append(Build.DEVICE)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Model: ")
        errorReport.append(Build.MODEL)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Id: ")
        errorReport.append(Build.ID)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Product: ")
        errorReport.append(Build.PRODUCT)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("\n*************** Android ****************\n")
        errorReport.append("SDK: ")
        errorReport.append(Build.VERSION.SDK_INT)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Release: ")
        errorReport.append(Build.VERSION.RELEASE)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Incremental: ")
        errorReport.append(Build.VERSION.INCREMENTAL)
        errorReport.append(LINE_SEPARATOR)

        // Show log of error

        Log.e(logTag, errorReport.toString())

        // Grava o log da exception

        try {

            // Directory of logs
            // TODO: port this

//            if (Util.diretorioLogs != null) {
//
//                val dirLogs = File(Util.diretorioLogs!!)
//
//                if (dirLogs.exists() == false) {
//                    dirLogs.mkdir()
//                }
//
//                var logExcecao = File(dirLogs, "Excecoes.txt")
//                if (!logExcecao.exists()) {
//                    logExcecao.createNewFile()
//                }
//                if (logExcecao.length() > 512000) { // Se for maior do que 512k, renomeia para ant e cria um novo
//                    val logAnt = File(dirLogs, "Excecoes_ant.txt")
//                    if (logAnt.exists()) {
//                        logAnt.delete()
//                    }
//                    logExcecao.renameTo(logAnt)
//                    logExcecao = File(dirLogs, "Excecoes.txt")
//                    logExcecao.createNewFile()
//                }
//
//                // Grava a exception
//
//                var fileOutputStream = FileOutputStream(logExcecao, true)
//                fileOutputStream.write(errorReport.toString().toByteArray())
//                fileOutputStream.close()
//
//                // Grava a ultima exception
//
//                logExcecao = File(dirLogs, "Excecao.txt")
//                if (logExcecao.exists()) {
//                    logExcecao.delete()
//                }
//                logExcecao.createNewFile()
//
//                fileOutputStream = FileOutputStream(logExcecao, true)
//                fileOutputStream.write(errorReport.toString().toByteArray())
//                fileOutputStream.close()
//
//            }

        } catch (e: Exception) {

        }

        // Show exception screen

        val intent = Intent(this, ExceptionActivity::class.java)

        if (message != null) {
            intent.putExtra("Message", message)
        } else if (exception != null) {
            intent.putExtra("Message", exception.message)
        }

        intent.putExtra("Exception", errorReport.toString())

        this.startActivity(intent)

    } catch (e: Exception) {
        e.printStackTrace()
    }

}
