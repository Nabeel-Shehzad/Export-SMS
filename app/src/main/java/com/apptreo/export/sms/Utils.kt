package com.apptreo.export.sms

import android.content.Context
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

object Utils {
    fun getJsonFromAssets(context: Context?, fileName: String?): String {
        val jsonString: String = try {
            val `is`: InputStream = FileInputStream(fileName)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
        return jsonString
    }
}