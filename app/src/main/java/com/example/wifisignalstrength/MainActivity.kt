// MainActivity.kt
package com.example.wifisignalstrength

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.TextView
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var signalStrengthText: TextView
    private lateinit var signalIcon: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // Update every second

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateWiFiSignal()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signalStrengthText = findViewById(R.id.signalStrengthText)
        signalIcon = findViewById(R.id.signalIcon)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        checkPermissionAndStartUpdates()
    }

    private fun checkPermissionAndStartUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startSignalUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSignalUpdates()
            }
        }
    }

    private fun startSignalUpdates() {
        handler.post(updateRunnable)
    }

    private fun updateWiFiSignal() {
        if (!wifiManager.isWifiEnabled) {
            signalStrengthText.text = "WiFi is disabled"
            signalIcon.setImageResource(R.drawable.ic_signal_wifi_off)
            return
        }

        val wifiInfo = wifiManager.connectionInfo
        val rssi = wifiInfo.rssi
        val signalLevel = WifiManager.calculateSignalLevel(rssi, 5)

        // Update signal strength text
        val signalQuality = when (signalLevel) {
            0 -> "Very Poor"
            1 -> "Poor"
            2 -> "Fair"
            3 -> "Good"
            4 -> "Excellent"
            else -> "Unknown"
        }

        signalStrengthText.text = "Signal Strength: $signalQuality\nRSSI: $rssi dBm"

        // Update signal icon
        val iconResource = when (signalLevel) {
            0 -> R.drawable.ic_signal_wifi_0
            1 -> R.drawable.ic_signal_wifi_1
            2 -> R.drawable.ic_signal_wifi_2
            3 -> R.drawable.ic_signal_wifi_3
            4 -> R.drawable.ic_signal_wifi_4
            else -> R.drawable.ic_signal_wifi_off
        }
        signalIcon.setImageResource(iconResource)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}