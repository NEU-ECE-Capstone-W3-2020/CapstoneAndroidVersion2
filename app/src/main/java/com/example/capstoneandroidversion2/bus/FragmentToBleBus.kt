package com.example.capstoneandroidversion2.bus

import android.bluetooth.le.ScanResult

data class FragmentToBleBus(
    val scanResult: ScanResult? = null,
    val shouldRead: Int? = null,
    val shouldWrite: String? = null,
    val shouldDisconnect: Int? = null
) {
}
