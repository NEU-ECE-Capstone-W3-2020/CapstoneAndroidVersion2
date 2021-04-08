package com.example.capstoneandroidversion2.model

import java.io.Serializable

data class NotificationMessage(
    val body: String,
    val subject: String,
    val timestamp: String,
    val lat: Double?,
    val long: Double?
) : Serializable {
}
