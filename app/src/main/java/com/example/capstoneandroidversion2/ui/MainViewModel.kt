package com.example.capstoneandroidversion2.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstoneandroidversion2.ble.BleManager
import com.example.capstoneandroidversion2.ble.BleRepository
import java.util.*

//TODO: Refactor to use the service as a means of communication
class MainViewModel : ViewModel() {

    // LiveData
    private val _viewState = MutableLiveData<MainViewState>().apply {
        value = MainViewState()
    }
    val viewState: LiveData<MainViewState> = _viewState

    // other variables
    private var bleManager: BleManager? = null

    fun pairDevice(
            deviceName: String,
            btAdapter: BluetoothAdapter,
            suuid: UUID,
            ruuid: UUID,
            wuuid: UUID,
            context: Context
    ) {
        bleManager = BleManager(deviceName, btAdapter, suuid, ruuid, wuuid, context).apply {
            startBleScan()
        }
    }

    fun connectToFoundDevice() {
        bleManager?.initiateConnection()
    }

    fun unPairDevice() {
        bleManager?.stopBleScan()
    }
}
