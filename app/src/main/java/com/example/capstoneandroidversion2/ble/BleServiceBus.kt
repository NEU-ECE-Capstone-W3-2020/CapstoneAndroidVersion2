package com.example.capstoneandroidversion2.ble

import android.bluetooth.le.ScanResult

data class BleServiceBus(
    val isDeviceFound: Boolean? = null,
    val isDeviceConnected: Boolean? = null,
    val currentReadValue: String? = null,
    val device: ScanResult? = null
) {
}