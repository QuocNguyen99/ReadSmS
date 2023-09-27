package com.company.readsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            Log.d("SmsReceiver", "onReceive: action ")
            val bundle = intent.extras
            if (bundle != null) {
                Log.d("SmsReceiver", "onReceive: bundle ")
                val pdus = bundle["pdus"] as Array<*>?
                if (pdus != null) {
                    for (pdu in pdus) {
                        val message = SmsMessage.createFromPdu(pdu as ByteArray)
                        val sender = message.originatingAddress
                        val messageBody = message.messageBody
                        Log.d("SmsReceiver", "onReceive: $messageBody ")
                        FirebaseManager.setSms(sender ?: "", messageBody)
                        Toast.makeText(context, "New message: $messageBody", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }
}