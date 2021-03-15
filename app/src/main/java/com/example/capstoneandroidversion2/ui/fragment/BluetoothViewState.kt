package com.example.capstoneandroidversion2.ui.fragment

import android.bluetooth.le.ScanResult

data class BluetoothViewState(
    val scanResult: ScanResult? = null,
    val paired: Boolean = false
)
