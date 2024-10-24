// MainActivity.kt
package com.example.wifisignalstrength

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var wifiAdapter: WifiNetworkAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val wifiList = mutableListOf<ScanResult>()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        setupRecyclerView()
        setupSwipeRefresh()
        checkPermissionAndStartScan()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        wifiAdapter = WifiNetworkAdapter(wifiList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = wifiAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            startWifiScan()
        }
    }

    private fun checkPermissionAndStartScan() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (permissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }) {
            startWifiScan()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun startWifiScan() {
        registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Please enable WiFi", Toast.LENGTH_LONG).show()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        wifiManager.startScan()
    }

    private fun scanSuccess() {
        wifiList.clear()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiList.addAll(wifiManager.scanResults)
        wifiAdapter.notifyDataSetChanged()
    }

    private fun scanFailure() {
        // Use cached results if available
        wifiList.clear()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiList.addAll(wifiManager.scanResults)
        wifiAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Scan failed. Showing cached results.", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startWifiScan()
            } else {
                Toast.makeText(this, "Permissions required to scan WiFi networks", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}