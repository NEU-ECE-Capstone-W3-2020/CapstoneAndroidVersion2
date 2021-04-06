package com.example.capstoneandroidversion2.ui

import android.bluetooth.le.ScanResult
import com.example.capstoneandroidversion2.model.NotificationMessage
import java.text.SimpleDateFormat
import java.util.*

data class MainViewState(
    val scanResult: ScanResult? = null,
    val paired: Boolean = false,
    val readPosts: List<NotificationMessage>? = null
) {
    //TODO: make this function check the repo for proper strings
    fun appendMessage(msg: String): MainViewState? {
        val newList = readPosts?.toMutableList() ?: mutableListOf()
        newList?.let {
            it.add(
                NotificationMessage(
                    msg,
                    "New Tag Found",
                    SimpleDateFormat("HH:mm").format(Date())
                )
            )
        }
        return this.copy(readPosts = newList)
    }
}
