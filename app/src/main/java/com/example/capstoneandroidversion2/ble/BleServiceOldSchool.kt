package com.example.capstoneandroidversion2.ble

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.capstoneandroidversion2.ui.fragment.READ_CHAR
import com.example.capstoneandroidversion2.ui.fragment.SERVICE_UUID
import com.example.capstoneandroidversion2.ui.fragment.WRITE_CHAR
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

/**
 * Working bluetooth background service we'll use to maintain a connection to our device
 */
class BleServiceOldSchool : Service() {
    private val bus: Bus = BusHolder.bus
    private lateinit var bleManager: BleManager
    private val mBinder = LocalBinder()
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    inner class LocalBinder : Binder() {
        val service: BleServiceOldSchool
            get() = this@BleServiceOldSchool
    }

    override fun onBind(intent: Intent?): IBinder? =
        mBinder

    override fun onCreate() {
        logger("Service Started")
        bus.register(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger("OnStartCommand")
        // first scan for devices
        bleManager = BleManager(
            "BEACON",
            bluetoothAdapter,
            SERVICE_UUID,
            READ_CHAR,
            WRITE_CHAR,
            this
        ).apply {
            startBleScan()
            setResultCallback { result ->
                bus.post(
                    BleServiceBus(
                        isDeviceFound = true,
                        device = result,
                        isDeviceConnected = null,
                        currentReadValue = null
                    )
                )
            }
        }
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        logger("ble service killed")
        return super.stopService(name)
    }

    private fun logger(s: String) {
        Log.wtf(this.javaClass.canonicalName, s)
    }

    /**
     * we use a communication bus to communicate from service to class
     */
    @Subscribe
    fun getFragmentEvent(event: FragmentToBleBus) {
        event.scanResult?.let {
            // saying its okay to begin connection
            bleManager.initiateConnection()

        }
        event.shouldRead?.let {
            bleManager.readValue()
        }
        event.shouldWrite?.let {
            bleManager.writeValue(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logger("DESTROYED")
        bleManager.disconnect()
    }


}