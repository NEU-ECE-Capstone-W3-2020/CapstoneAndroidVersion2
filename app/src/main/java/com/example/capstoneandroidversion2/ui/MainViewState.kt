package com.example.capstoneandroidversion2.ui

import android.bluetooth.le.ScanResult

data class MainViewState(
        val scanResult: ScanResult? = null,
        val paired: Boolean = false
)
