package com.example.capstoneandroidversion2.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.example.capstoneandroidversion2.bus.BleServiceBus
import com.example.capstoneandroidversion2.bus.BusHolder
import com.example.capstoneandroidversion2.model.NotificationMessage
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import java.nio.charset.Charset
import java.util.*

class BleManager(context: Context, postNotification: (NotificationMessage) -> Unit) : ObservableBleManager(context) {

    companion object {
        private val SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        private val READ_CHAR = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        private val WRITE_CHAR = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private var correctGatt: BluetoothGatt? = null
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var supported = false


    override fun getGattCallback(): BleManagerGattCallback {
        return BleManagerGattCallbackImpl()
    }

    private val readCallback = object : DataSentCallback, DataReceivedCallback {
        override fun onDataSent(device: BluetoothDevice, data: Data) {
            //readState.postValue(data.value?.toString(Charset.defaultCharset()))
            //TODO: do we need to to anything here?
        }

        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            //readState.postValue(data.value?.toString(Charset.defaultCharset()))
            BusHolder.bus.post(BleServiceBus(currentReadValue = data.value?.toString(Charset.defaultCharset())))
            //TODO: fix this so it ignores repeat messages, or properly handles messages of a longer length
            postNotification(NotificationMessage(body = data.value?.toString(Charset.defaultCharset()) ?: "error reading data value", subject = "New Tag Discovered!"))
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
