package com.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val prefs = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(MainActivity.KEY_ENABLED, false)
        val botToken = prefs.getString(MainActivity.KEY_BOT_TOKEN, "") ?: ""
        val chatId = prefs.getString(MainActivity.KEY_CHAT_ID, "") ?: ""
        if (!enabled || botToken.isEmpty() || chatId.isEmpty()) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullMessage = StringBuilder()
        var sender = ""
        for (sms in messages) {
            sender = sms.displayOriginatingAddress ?: "Unknown"
            fullMessage.append(sms.messageBody)
        }
        val timeNow = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val msg = "SMS Masuk\nDari: $sender\nWaktu: $timeNow\nPesan:\n${fullMessage}"
        Thread { TelegramSender.send(botToken, chatId, msg) }.start()
    }
}
