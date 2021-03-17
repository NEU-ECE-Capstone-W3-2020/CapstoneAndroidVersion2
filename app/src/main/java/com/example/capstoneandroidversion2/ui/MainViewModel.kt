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

//TODO: We need to change the scope of this to make our bluetooth manager independent of the viewmodel
class MainViewModel : ViewModel() {

    // LiveData
    private val _isPaired = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isPaired: LiveData<Boolean> = _isPaired

    private val _viewState = MutableLiveData<MainViewState>().apply {
        value = MainViewState()
    }
    val viewState: LiveData<MainViewState> = _viewState

    // Observers
    private val characteristicObserver = androidx.lifecycle.Observer<String?> {
        it?.let { handleCharacteristicChange(it) }
    }
    private val gattObserver = androidx.lifecycle.Observer<BluetoothGatt?> {
        _isPaired.postValue(it != null)
    }

    private val scanResultObserver = androidx.lifecycle.Observer<ScanResult?> {
        it?.let { _viewState.postValue(_viewState.value?.copy(scanResult = it)) }
    }

    // other variables
    private var bleManager: BleManager? = null


    // functions
    private fun handleCharacteristicChange(newChar: String) {
        BleRepository.messageStack.push(newChar)
    }

    fun pairDevice(
            deviceName: String,
            btAdapter: BluetoothAdapter,
            suuid: UUID,
            ruuid: UUID,
            wuuid: UUID,
            context: Context
    ) {
        bleManager = BleManager(deviceName, btAdapter, suuid, ruuid, wuuid, context).apply {
            correctScanResultLiveData.observeForever(scanResultObserver)
            characteristicValueLiveData.observeForever(characteristicObserver)
            gattDeviceLiveData.observeForever(gattObserver)
            startBleScan()
        }
    }

    fun connectToFoundDevice() {
        bleManager?.initiateConnection()
    }

    fun unPairDevice() {
        bleManager?.stopBleScan()
    }

    // lifecycle
    override fun onCleared() {
        super.onCleared()
        bleManager?.apply {
            correctScanResultLiveData.removeObserver(scanResultObserver)
            characteristicValueLiveData.removeObserver(characteristicObserver)
            gattDeviceLiveData.removeObserver(gattObserver)
        }

    }
}
