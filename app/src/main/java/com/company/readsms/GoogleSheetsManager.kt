package com.company.readsms

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.IOException
import java.io.InputStream
import java.util.Collections

class GoogleSheetsManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSheetsManager"
    }

    private lateinit var sheetsService: Sheets
    private lateinit var spreadsheetId: String

    init {
        initializeSheetsService()
    }

    private fun initializeSheetsService() {
        try {
            val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

            val scopes = listOf(SheetsScopes.SPREADSHEETS)

            val credential: GoogleCredential = usingOAuth2(
                context,
                scopes
            ).let { gac ->

                val resourceId = R.raw.cred
                val inputStream = context.resources.openRawResource(resourceId)

                val cre = GoogleCredential.fromStream(inputStream).createScoped(scopes)
                cre
            }

            sheetsService = Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName(context.getString(R.string.app_name))
                .build()

            spreadsheetId = "10616inp-X13GJqTlGFi0WBYlvfvDcLKLpUkfLYqTLmc"
        } catch (ex: Exception) {
            Log.e(TAG, "initializeSheetsService error:  ${ex.message}")
        }
    }

    fun addDataToSheet(sender: String, message: String) {
        val sheetName = "Sheet1"
        val range = "$sheetName!A:B"
        val values = listOf(listOf(sender, message))
        val valueRange = ValueRange().setValues(values)

        try {
            val appendRequest = sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, valueRange)
            appendRequest.valueInputOption = "USER_ENTERED"
            val response = appendRequest.execute()

            if (response.updates.updatedCells > 0) {
            } else {
                // Có lỗi xảy ra khi thêm dữ liệu
                // Xử lý lỗi ở đây
            }
        } catch (e: GoogleJsonResponseException) {
            // Xử lý lỗi ở đây
            Log.e(TAG, "addDataToSheet GoogleJsonResponseException: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "addDataToSheet exception: ${e.message}")
        }
    }

    fun getData(): List<Any>? {

        val spreadsheetId = "10616inp-X13GJqTlGFi0WBYlvfvDcLKLpUkfLYqTLmc" // id của file google của bạn
        val range = "Sheet1!A1:C2" // Phạm vi bạn muốn đọc dữ liệu ở sheet google
        val scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)

        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

        val credential: GoogleCredential = usingOAuth2(
            context,
            scopes
        ).let { gac ->

            val resourceId = R.raw.cred
            val inputStream = context.resources.openRawResource(resourceId)

            val cre = GoogleCredential.fromStream(inputStream).createScoped(scopes)
            cre
        }
        return try {
            credential.refreshToken()

            val service = Sheets.Builder(AndroidHttp.newCompatibleTransport(), jsonFactory, credential)
            val response = service.build().spreadsheets().values()
                .get(spreadsheetId, range).execute()
            Log.d(TAG, "getData: response.getValues: ${response.getValues()}")
            return response.getValues()
        } catch (e: Exception) {
            Log.e(TAG, "getData: ${e.message}")
            return emptyList()
        }
    }
}