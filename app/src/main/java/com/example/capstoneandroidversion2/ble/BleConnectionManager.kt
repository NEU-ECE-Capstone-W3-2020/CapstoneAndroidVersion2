package com.example.capstoneandroidversion2.ble

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log

class BleConnectionManager(
    val deviceName: String,
    private val bluetoothAdapter: BluetoothAdapter
) {

    private lateinit var mCallback: (ScanResult) -> Unit
    private val logTag: String = "BleManager"
    private var correctScanResult: ScanResult? = null

    // BLUETOOTH SCANNING
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i(logTag, "Name: ${name ?: "Unnamed"}, address: $address")
                if (name == deviceName && name != correctScanResult?.device?.name ?: "") {
                    correctScanResult = result
                    mCallback(result)
                    Log.i(logTag, "Found ${result.device.name}")
                    stopBleScan()
                }
            }
        }
    }
    private var isScanning = false
        set(value) {
            field = value
            Log.i(logTag, if (value) "Started Scan" else "Stopped Scan")
        }

    fun startBleScan() {
        bleScanner.startScan(null, scanSettings, scanCallback)
        isScanning = true
    }

    fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    fun setResultCallback(callback: (ScanResult) -> Unit) {
        mCallback = callback
    }
}
