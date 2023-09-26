package com.company.readsms

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCheckAndGetSms()
    }

    private fun getCheckAndGetSms() {
        if (checkSelfPermission(android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "No permission")
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_SMS), PackageManager.PERMISSION_GRANTED
            )
        } else {
            Log.d("MainActivity", "Have permission")

            val latestSms = getLatestSmsContent(contentResolver)

            if (latestSms != null) {
                Toast.makeText(this, "$latestSms", Toast.LENGTH_LONG).show()
                Log.d("MainActivity", "latestSms: $latestSms ")
                sendDataToGoogleSheet("BANK", latestSms)
            } else {
                Toast.makeText(this, "No mess", Toast.LENGTH_LONG).show()
                Log.d("MainActivity", "No mess")
            }
        }
    }

    private fun getLatestSmsContent(contentResolver: ContentResolver): String? {
        val uri = Uri.parse("content://sms")
        val cursor = contentResolver.query(
            uri,
            arrayOf("body"),
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val messageBodyIndex = it.getColumnIndex("body")
                return it.getString(messageBodyIndex)
            }
        }
        return null
    }

    private fun sendDataToGoogleSheet(title: String, message: String) {
        val queue = Volley.newRequestQueue(this)
        val urlTemp =
            "https://script.google.com/macros/s/AKfycbyAPJme7tjHPWSco3191VqiSIiYjF7YfrKIbHq0XoPWjr4dw_J4zXNSJBWzhj1UnTI1LQ/exec"
//        urlTemp + "?action=create&title" + title + "&message" + message

        val stringRequest = object : StringRequest(Request.Method.GET, urlTemp,
            Response.Listener<String> { response ->
                Log.d("MainActivity", "response: $response")
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener { error ->
                Log.e("MainActivity", "error: ${error.printStackTrace()}")
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
            }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["action"] = "create"
                params["title"] = title
                params["message"] = message

                return params
            }
        }

        queue.add(stringRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getCheckAndGetSms()
    }
}
