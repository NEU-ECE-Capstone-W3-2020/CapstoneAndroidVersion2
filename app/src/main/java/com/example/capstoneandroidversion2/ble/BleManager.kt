package com.example.capstoneandroidversion2.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.res.Resources
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.model.NotificationMessage
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class BleManager(
    context: Context,
    val resources: Resources,
    postNotification: (NotificationMessage) -> Unit
) :
    ObservableBleManager(context) {

    companion object {
        private val SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        private val READ_CHAR = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        private val WRITE_CHAR = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private var correctGatt: BluetoothGatt? = null
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var supported = false
    private var lastReadValue = ""


    override fun getGattCallback(): BleManagerGattCallback {
        return BleManagerGattCallbackImpl()
    }

    private val readCallback = object : DataSentCallback, DataReceivedCallback {
        override fun onDataSent(device: BluetoothDevice, data: Data) {}
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            // first we check if we have already read this
            val msg = data.value?.toString(Charset.defaultCharset())?.removeSuffix("\n") ?: "error"
            val notification =
                // checking if its a special character
                if (msg.isNumber()) {
                    NotificationMessage(
                        body = resources.getString(
                            msg.toResourceString()
                        )
                            ?: "error reading data value",
                        subject = "New Tag Discovered!",
                        timestamp = SimpleDateFormat("HH:mm").format(Date()),
                        lat = null,
                        long = null
                    )
                } else {
                    NotificationMessage(
                        body = msg
                            ?: "error reading data value",
                        subject = "New Tag Discovered!",
                        timestamp = SimpleDateFormat("HH:mm").format(Date()),
                        lat = null,
                        long = null
                    )
                }
            // right now we get spammed with messages, so this line prevents us from repeats
            if (msg != lastReadValue) {
                lastReadValue = msg
                postNotification(notification)
            }
        }
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private inner class BleManagerGattCallbackImpl : BleManagerGattCallback() {
        override fun initialize() {
            setNotificationCallback(readCharacteristic).with(readCallback)
            readCharacteristic(readCharacteristic).with(readCallback).enqueue()
            enableNotifications(readCharacteristic).enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            correctGatt = gatt
            val service = gatt.getService(SERVICE)
            if (service != null) {
                readCharacteristic = service.getCharacteristic(READ_CHAR)
                writeCharacteristic = service.getCharacteristic(WRITE_CHAR)
            }
            var writeRequest = false
            if (writeCharacteristic != null) {
                val rxProperties = writeCharacteristic!!.properties
                writeRequest = rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
            }
            supported = readCharacteristic != null && writeCharacteristic != null && writeRequest
            return supported
        }

        override fun onDeviceDisconnected() {
            readCharacteristic = null
            writeCharacteristic = null
        }
    }
}

private fun String?.toResourceString(): Int =
    when (this?.toIntOrNull()) {
        0 -> R.string.tag_0
        1 -> R.string.tag_1
        2 -> R.string.tag_2
        3 -> R.string.tag_3
        4 -> R.string.tag_4
        5 -> R.string.tag_5
        6 -> R.string.tag_6
        7 -> R.string.tag_7
        else -> R.string.error
    }

private fun String?.isNumber(): Boolean =
    this?.toIntOrNull() != null
