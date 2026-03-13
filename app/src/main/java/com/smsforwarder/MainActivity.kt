package com.smsforwarder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etBotToken: EditText
    private lateinit var etChatId: EditText
    private lateinit var switchForward: Switch
    private lateinit var btnSave: Button
    private lateinit var tvStatus: TextView
    private lateinit var btnTest: Button

    companion object {
        const val PREF_NAME = "SmsForwarderPrefs"
        const val KEY_BOT_TOKEN = "bot_token"
        const val KEY_CHAT_ID = "chat_id"
        const val KEY_ENABLED = "enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etBotToken = findViewById(R.id.etBotToken)
        etChatId = findViewById(R.id.etChatId)
        switchForward = findViewById(R.id.switchForward)
        btnSave = findViewById(R.id.btnSave)
        tvStatus = findViewById(R.id.tvStatus)
        btnTest = findViewById(R.id.btnTest)
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        etBotToken.setText(prefs.getString(KEY_BOT_TOKEN, "8730367664:AAGJR_y5JagSD8fH3X71cKCo6SgeH8DL8-g"))
        etChatId.setText(prefs.getString(KEY_CHAT_ID, "7927165897"))
        switchForward.isChecked = prefs.getBoolean(KEY_ENABLED, false)
        updateStatus()
        btnSave.setOnClickListener {
            val token = etBotToken.text.toString().trim()
            val chatId = etChatId.text.toString().trim()
            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "Isi Token dan Chat ID dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString(KEY_BOT_TOKEN, token).putString(KEY_CHAT_ID, chatId)
                .putBoolean(KEY_ENABLED, switchForward.isChecked).apply()
            if (switchForward.isChecked) startForwarderService() else stopForwarderService()
            Toast.makeText(this, "Tersimpan!", Toast.LENGTH_SHORT).show()
            updateStatus()
        }
        btnTest.setOnClickListener {
            val token = etBotToken.text.toString().trim()
            val chatId = etChatId.text.toString().trim()
            Thread { TelegramSender.send(token, chatId, "SMS Forwarder Aktif!") }.start()
            Toast.makeText(this, "Pesan test dikirim!", Toast.LENGTH_SHORT).show()
        }
        switchForward.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply()
            if (isChecked) startForwarderService() else stopForwarderService()
            updateStatus()
        }
        requestPermissions()
    }

    private fun updateStatus() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        tvStatus.text = if (enabled) "Aktif - SMS diteruskan ke Telegram" else "Nonaktif"
    }

    private fun startForwarderService() {
        val intent = Intent(this, ForwarderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun stopForwarderService() {
        stopService(Intent(this, ForwarderService::class.java))
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty())
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
    }
}
