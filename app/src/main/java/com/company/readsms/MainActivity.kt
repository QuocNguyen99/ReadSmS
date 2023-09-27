package com.company.readsms

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        getCheckAndGetSms()
        FirebaseManager.getNewSms(onCallback = {
            findViewById<TextView>(R.id.tv).apply {
                text = "Sender: ${it.first} \n" +
                        "Message: ${it.second} \n" +
                        "Time: ${it.third} \n"
            }
        })
    }

    private fun getCheckAndGetSms() {
        if (checkSelfPermission(android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainActivity", "No permission")
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.RECEIVE_SMS
                ), PackageManager.PERMISSION_GRANTED
            )
        } else {
            Log.d("MainActivity", "Have permission")

            val latestSms = getLatestSmsContent(contentResolver)

            Toast.makeText(this, "$latestSms", Toast.LENGTH_LONG).show()
            Log.d("MainActivity", "latestSms: $latestSms ")
            FirebaseManager.setSms(latestSms.first ?: "", latestSms.second ?: "")
        }
    }

    private fun getLatestSmsContent(contentResolver: ContentResolver): Pair<String?, String?> {
        val uri = Uri.parse("content://sms")
        val cursor = contentResolver.query(
            uri,
            arrayOf("address", "body"),
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val senderIndex = cursor.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                Log.d("MainActivity", "senderIndex: $senderIndex -- bodyIndex: $bodyIndex")
                val sender = it.getString(senderIndex)
                val body = it.getString(bodyIndex)
                Log.d("MainActivity", "sender: $sender -- body: $body")

                return sender to body
            }
        }
        cursor?.close()

        return null to null
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
