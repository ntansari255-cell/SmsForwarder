package com.smsforwarder

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TelegramSender {
    fun send(botToken: String, chatId: String, message: String) {
        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val urlStr = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMsg"
            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.connect()
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
