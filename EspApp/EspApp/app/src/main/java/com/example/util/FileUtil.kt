/* ***********
 * Project   : Esp-App-Mobile-Android - App to connect a Esp32 device by BLE
 * Programmer: Joao Lopes
 * Module    : Util - utilities
 * Comments  : FileUtil - Routines to files
 * Versions  :
 ------- -------- -------------------------
 * 0.1.0    20/08/18    First version
 **/

package com.example.utilitaries

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.support.v4.content.ContextCompat
import com.example.util.Fields
import com.example.util.logE
import java.io. *
import java.util. *

object FileUtil {

    fun getFile(path: String): File {

        // Open File object for file

        return File (path)
    }

    @Throws(Exception::class)
    fun getArquivoExistente(path: String): File {

        // Open File object for the file that already exists

        val fArquivo = getFile (path)
        if (!fArquivo.isFile) {
            throw Exception ("File does not exist: $path")
        }
        return fArquivo
    }

    @Throws(Exception::class)
    fun getNewFile(path: String): File {

        // Open File object for file, which should not exist

        val fArquivo = getFile (path)
        if (fArquivo.isFile == true) {
            throw Exception ("File already exists: $path")
        }
        return fArquivo
    }

    @Throws(IOException::class)
    fun copyFile (source: String, target: String, overwrite: Boolean = false) {

        // Copy a file

        File(source).copyTo (File(target), overwrite)

    }

    @Throws(Exception::class)
    fun createDirectory(path: String) {

        // Create a directory

        val directory = File(path)

        if (directory.exists ()) {
            if (directory.isDirectory) {
                return
            } else {
                throw Exception ("Could not create the directory, because file with the same directory name already exists.")
            }
        } else {

            directory.mkdirs ()
        }
    }

    fun deleteFiles (directory: File, recursive: Boolean) {

        // Delete files from a directory

        if (directory.isDirectory && directory.exists ()) {

            for (file in directory.listFiles ()) {

                if (file.isFile) {
                    file.delete ()
                } else if (directory.isDirectory && recursive) {
                    deleteFiles (file, recursive)
                    file.delete ()
                }
            }
        }
    }

    fun isDiretory(path: String): Boolean {

        // Return if directory and if exists

        val fDir = File(path)

        return fDir.exists() && fDir.isDirectory

    }

    fun isFile(path: String): Boolean {

        // Returns if and only the file exists

        try {
            val fDir = File(path)

            return fDir.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getFileSize(path: String): Long {

        return try {
            val file = File(path)
            if (file.exists()) {
                file.length()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }

    }

    fun getMbFreeSpace(path: String): Double {

        // Return available space in Mb - based on http://stackoverflow.com/questions/16834964/how-to-get-an-external-storage-sd-card-size-with-mounted-sd-card
        val stat = StatFs(path)
        //        double sdAvailSize = (double)stat.getAvailableBlocks()
        //                * (double)stat.getBlockSize();
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        return (bytesAvailable / (1024f * 1024f)).toDouble()
        //      double megaBytes  = sdAvailSize / 1048576;
        //        return megaBytes;
    }

    // Search directory to app put files
    // Search by more space free
    // Experimental code

    fun searchDirectory (context: Context, packageName: String): String? {

        try {

            // Return the directory of the android that has this directory or that has more space

            val arrayDirs = ArrayList<String>()

            // // Private -> Application data directory - TODO: see error in <= 5.0
            //
            //  arrayDirs.add(Util.getApplicationInfo().dataDir);

            // Storage directories

            val fileDirStorages = ContextCompat.getExternalFilesDirs (context, null)

            for (fileDirStorage in fileDirStorages) {
                if (fileDirStorage != null) {
                    arrayDirs.add(fileDirStorage.path)
                }
            }

            // For Android version smaller than KitKat (<4.4)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

                // Directory

                val dirAndroid = File.separator + "Android" + File.separator + "data" + File.separator + packageName

                // Public -> Storages

                var storage: String?

                storage = Environment.getExternalStorageDirectory (). toString ()
                if (storage.indexOf(":") > 0) {
                    val fields = Fields(storage, ":");
                    storage = fields.getField(1)
                }
                arrayDirs.add("$storage$dirAndroid")

                storage = System.getenv("SECONDARY_STORAGE")
                if (storage != null) {
                    if (storage.indexOf(":") > 0) {
                        val fields = Fields(storage, ":");
                        storage = fields.getField(1)
                    }
                    arrayDirs.add("$storage$dirAndroid")
                }

                storage = System.getenv("EXTERNAL_STORAGE")

                if (storage != null) {
                    arrayDirs.add("$storage$dirAndroid")
                }

            }

            // Add files directory

            for (i in arrayDirs.indices) {

                if (!arrayDirs[i].toLowerCase().endsWith("files")) {
                    arrayDirs[i] = arrayDirs[i] + File.separator + "files"
                }
            }

            // Delete repeated (https://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist)

            val aux = HashSet<String>()
            aux.addAll (arrayDirs)
            arrayDirs.clear()
            arrayDirs.addAll (aux)

            // Check where it is installed

            for (directory in arrayDirs) {
                if (File(directory + File.separator + "app.dat").exists()) {
                    return directory
                }
            }

            // Does not exist -> Checks free space

            var greaterSpace = 0.0
            var posGreaterSpace = 0

            for (i in arrayDirs.indices) {

                val space = getMbFreeSpace(arrayDirs[i].replace(File.separator + "files", ""))

                if (space > greaterSpace) {
                    greaterSpace = space
                    posGreaterSpace = i
                }
            }

            // At least 250 Mb

            if (greaterSpace < 250) {

                return null

            }

            // Return the directory in storage

            val ret = File (arrayDirs [posGreaterSpace])
            ret.mkdirs ()

            val check = File (ret.path + File.separator + "Test.txt")
            try {
                check.createNewFile()
                check.delete()
            } catch (e: IOException) {
                // TODO: see this
                // activity !!. extShowExpression ("Could not access directory:" + return.absolutePath, e)
                logE ("Could not access directory: ${ret.absolutePath}")
                e.printStackTrace()
                return null
            }

            return ret.path

        } catch (e: Exception) {
// activity !!. extShowExpression (e)
            logE("", e)
            return null
        }
    }
}