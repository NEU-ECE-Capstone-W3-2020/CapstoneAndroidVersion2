package com.example.capstoneandroidversion2.ui

import android.bluetooth.le.ScanResult
import com.example.capstoneandroidversion2.model.NotificationMessage

data class MainViewState(
    val scanResult: ScanResult? = null,
    val paired: Boolean = false,
    val readPosts: List<NotificationMessage>? = null
) {

    fun appendMessage(msg: NotificationMessage): MainViewState? {
        val newList = readPosts?.toMutableList() ?: mutableListOf()
        if (!newList.contains(msg)) {
            newList.add(msg)
        }
        return this.copy(readPosts = newList)
    }
}
