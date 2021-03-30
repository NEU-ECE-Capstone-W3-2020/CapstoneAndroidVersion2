package com.example.capstoneandroidversion2.ble

import android.bluetooth.le.ScanResult

data class FragmentToBleBus(
    val scanResult: ScanResult? = null,
    val shouldRead: Int? = null,
    val shouldWrite: String? = null
) {
}
