package com.example.capstoneandroidversion2.bus

import android.bluetooth.le.ScanResult

data class BleServiceBus(
    val isDeviceFound: Boolean? = null,
    val isDeviceConnected: Boolean? = null,
    val currentReadValue: String? = null,
    val device: ScanResult? = null,
    val shouldKillService: Int? = null
) {
}
