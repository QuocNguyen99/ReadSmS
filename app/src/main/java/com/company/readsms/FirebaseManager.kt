package com.company.readsms

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseManager {
    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore

    fun setSms(title: String, message: String) {
        val sdf = SimpleDateFormat("dd MM yyyy HH:mm", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        val formattedDate = sdf.format(date)

        val data = hashMapOf("sender" to title, "message" to message, "time" to formattedDate)
        db.collection("sms")
            .add(data)
            .addOnSuccessListener {
                Log.d(
                    "FirebaseManager",
                    "DocumentSnapshot successfully written!"
                )
            }
            .addOnFailureListener { e -> Log.w("FirebaseManager", "Error writing document", e) }
    }

    fun getNewSms(onCallback: (data: Triple<String?, String?, String?>) -> Unit) {
        db.collection("sms")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("FirebaseManager", "getNewSms")
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {

                        Log.d("FirebaseManager", "New sms: ${dc.document.data}")
                        val sender = dc.document.data["sender"] as? String
                        val time = dc.document.data["time"] as? String
                        val message = dc.document.data["message"] as? String
                        onCallback(Triple(sender, message, time))
                    }
                }
            }
    }
}