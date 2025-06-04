package com.ally.smsforward

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.ally.smsforward.data.AppDatabase
import com.ally.smsforward.data.LogEntry
import com.ally.smsforward.network.RetrofitClient
import com.ally.smsforward.network.SmsForwardRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val sharedPreferences = context.getSharedPreferences("SmsForwardPrefs", Context.MODE_PRIVATE)
            val isListening = sharedPreferences.getBoolean("isListening", false)

            if (!isListening) {
                Log.d("SmsReceiver", "Not listening for SMS.")
                return
            }

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val smsKeyword = sharedPreferences.getString("smsKeyword", "") ?: ""
            val wechatNumber = sharedPreferences.getString("wechatNumber", "") ?: ""

            if (smsKeyword.isBlank()) {
                Log.d("SmsReceiver", "SMS keyword is not set.")
                return
            }

            for (smsMessage in messages) {
                val sender = smsMessage.displayOriginatingAddress
                val messageBody = smsMessage.messageBody
                // In a real scenario, the 'recipient' would be the phone number the SMS was sent to.
                // For this example, we might not have it directly or might use a placeholder.
                if (messageBody.contains(smsKeyword)) {
                    Log.d("SmsReceiver", "SMS received from: $sender, message: $messageBody, forwarding to: $wechatNumber")
                    forwardSmsToServer(context, sender, messageBody, wechatNumber)
                }
            }
        }
    }

    private fun forwardSmsToServer(context: Context, sender: String, message: String, wechatNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val logDao = AppDatabase.getDatabase(context.applicationContext).logDao()
            var status = "FAILURE"
            try {
                val request = SmsForwardRequest(sender, message, wechatNumber)
                val response = RetrofitClient.instance.forwardSms(request)
                if (response.isSuccessful) {
                    Log.d("SmsReceiver", "SMS forwarded successfully to server.")
                    status = "SUCCESS"
                } else {
                    Log.e("SmsReceiver", "Failed to forward SMS. Code: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error forwarding SMS: ${e.message}", e)
            }
            // Log the attempt
            val logEntry = LogEntry(
                timestamp = System.currentTimeMillis(),
                sender = sender,
                message = message,
                wechatNumber = wechatNumber,
                status = status
            )
            logDao.insert(logEntry)
        }
    }
}