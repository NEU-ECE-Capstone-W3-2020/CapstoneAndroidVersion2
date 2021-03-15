package com.example.capstoneandroidversion2.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstoneandroidversion2.ui.ble.BleManager
import com.example.capstoneandroidversion2.ui.ble.BleRepository
import java.util.*

//TODO: We need to change the scope of this to make our bluetooth manager independent of the viewmodel
class BluetoothViewModel : ViewModel() {

    private val characteristicObserver = androidx.lifecycle.Observer<String?> {
        it?.let { handleCharacteristicChange(it) }
    }

    private fun handleCharacteristicChange(newChar: String) {
        BleRepository.messageStack.push(newChar)
    }

    private val _viewState = MutableLiveData<BluetoothViewState>().apply {
        value = BluetoothViewState()
    }
    val viewState: LiveData<BluetoothViewState> = _viewState

    private var bleManager: BleManager? = null
    private val scanResultObserver = androidx.lifecycle.Observer<ScanResult?> {
        it?.let { _viewState.postValue(_viewState.value?.copy(scanResult = it)) }
    }
    private val gattObserver = androidx.lifecycle.Observer<BluetoothGatt?> {
        _isPaired.postValue(it != null)
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

    fun unPairDevice() {
        bleManager?.stopBleScan()
    }


    private val _isPaired = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isPaired: LiveData<Boolean> = _isPaired
    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    override fun onCleared() {
        super.onCleared()
        bleManager?.apply {
            correctScanResultLiveData.removeObserver(scanResultObserver)
            characteristicValueLiveData.removeObserver(characteristicObserver)
            gattDeviceLiveData.removeObserver(gattObserver)
        }

    }

    fun connectToFoundDevice() {
        bleManager?.initiateConnection()
    }
}
