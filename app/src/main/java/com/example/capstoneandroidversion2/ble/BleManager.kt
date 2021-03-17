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

    private val logTag: String = "BleManager"
    private var correctScanResult: ScanResult? = null
    private var correctGatt: BluetoothGatt? = null
    private var _correctScanResultLiveData = MutableLiveData<ScanResult?>().apply {
        value = null
    }
    val correctScanResultLiveData: LiveData<ScanResult?> = _correctScanResultLiveData
    private val _characteristicValue = MutableLiveData<String?>()
    val characteristicValueLiveData: LiveData<String?> = _characteristicValue
    private val _gattDevice = MutableLiveData<BluetoothGatt?>()
    val gattDeviceLiveData: LiveData<BluetoothGatt?> = _gattDevice

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
            }
            if (result.device.name == deviceName && result.device.name != correctScanResult?.device?.name ?: "") {
                correctScanResult = result
                _correctScanResultLiveData.postValue(result)
                Log.i(logTag, "Found ${result.device.name}")
                stopBleScan()
            }
        }
    }
    private var isScanning = false
        set(value) {
            field = value
            Log.i(logTag, if (value) "Stopped Scan" else "Started Scan")
        }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    /*Toast.makeText(this@MainActivity, "Found ${gatt.device}", Toast.LENGTH_LONG)
                        .show()*/
                    correctGatt = gatt.apply {
                        discoverServices()
                        _gattDevice.postValue(this)
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                    _gattDevice.postValue(null)
                }
            } else {
                Log.w(
                        "BluetoothGattCallback",
                        "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
                _gattDevice.postValue(null)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gatt?.services?.forEach {
                it.characteristics.forEach {
                    if (it.uuid == read_char) {
                        //appendText("FOUND CORRECT READ CHARACTERISTIC")
                    }
                }

                if (it.uuid == service_uuid) {
                    // should enable live update on characteristic change
                    with(gatt) {
                        readCharacteristic(it.getCharacteristic(read_char))
                        setCharacteristicNotification(
                                getService(service_uuid).getCharacteristic(
                                        read_char
                                ), true
                        )
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
                        //appendText("BEACON VALUE: ${this?.value?.toString(Charset.defaultCharset())}")
                        BleRepository.messageStack.push(this?.value?.toString(Charset.defaultCharset()))
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        //appendText("read not permitted")
                    }
                    else -> {
                        //appendText("read failed")
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
                // add a newly updated message to the queue
                BleRepository.messageStack.push(value.toString(Charset.defaultCharset()))
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
                        //appendText("wrote ${value!!.toString(Charset.defaultCharset())} to ${gatt!!.device.name}")
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
        val char = gatt.getService(service_uuid).getCharacteristic(read_char)
        gatt.readCharacteristic(char)
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
}
