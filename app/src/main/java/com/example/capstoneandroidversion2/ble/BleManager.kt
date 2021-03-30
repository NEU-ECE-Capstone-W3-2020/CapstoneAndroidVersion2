package com.example.capstoneandroidversion2.ble

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.nio.charset.Charset
import java.util.*

class BleManager(
    val deviceName: String,
    val bluetoothAdapter: BluetoothAdapter,
    val service_uuid: UUID,
    val read_char: UUID,
    val write_char: UUID,
    val context: Context
) {

    // first we need to register this as a bus listener for communication
    init {
        BusHolder.bus.register(this)
    }

    private lateinit var mCallback: (ScanResult) -> Unit
    private val logTag: String = "BleManager"
    private var correctScanResult: ScanResult? = null
    private var correctGatt: BluetoothGatt? = null
    private var correctReadChar: BluetoothGattCharacteristic? = null

    // BLUETOOTH SCANNING
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i(logTag, "Name: ${name ?: "Unnamed"}, address: $address")
                if (name == deviceName && name != correctScanResult?.device?.name ?: "") {
                    correctScanResult = result
                    mCallback(result)
                    Log.i(logTag, "Found ${result.device.name}")
                    stopBleScan()
                }
            }
        }
    }
    private var isScanning = false
        set(value) {
            field = value
            Log.i(logTag, if (value) "Started Scan" else "Stopped Scan")
        }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    correctGatt = gatt.apply {
                        discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gatt?.services?.forEach { service ->
                service.characteristics.forEach { char ->
                    // THIS IS THE CORRECT CHARACTERISTIC WHICH WE CAN READ FROM, has no descriptors
                    if (char.uuid == read_char && char.descriptors.isEmpty()) {
                        correctReadChar = char
                        gatt.readCharacteristic(correctReadChar)
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        val value = this?.value?.toString(Charset.defaultCharset())
                        BleRepository.messageStack.push(value)
                        BusHolder.bus.post(BleServiceBus(currentReadValue = value))
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.i(logTag, "Characteristic changed: $characteristic")
            characteristic?.apply {
                //TODO: Not currently functional, dependent on Firmware
                // add a newly updated message to the queue
                BleRepository.messageStack.push(value.toString(Charset.defaultCharset()))
                BusHolder.bus.post(BleServiceBus(currentReadValue = value.toString(Charset.defaultCharset())))
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            characteristic?.apply {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(logTag, "Successfully wrote to device")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    }
                    else -> {
                    }
                }
            }
        }
    }


    fun startBleScan() {
        bleScanner.startScan(null, scanSettings, scanCallback)
        isScanning = true
    }

    fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    fun initiateConnection() {
        correctScanResult?.device?.let {
            it.connectGatt(context, false, gattCallback)
        }
    }

    private fun readFromGatt(gatt: BluetoothGatt) {
        gatt.readCharacteristic(correctReadChar)
    }

    private fun writeToGatt(gatt: BluetoothGatt, text: String) {
        writeCharacteristic(
            gatt.getService(service_uuid).getCharacteristic(write_char), text.toByteArray(
                Charset.defaultCharset()
            )
        )
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        correctGatt?.let { gatt ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }

    fun setResultCallback(callback: (ScanResult) -> Unit) {
        mCallback = callback
    }

    // writes to event bus
    fun readValue() {
        correctGatt?.let { readFromGatt(it) }
    }

    fun disconnect() {
        correctGatt?.close()
    }

    fun writeValue(s: String) {
        correctGatt?.let { writeToGatt(it, s) }
    }
}
