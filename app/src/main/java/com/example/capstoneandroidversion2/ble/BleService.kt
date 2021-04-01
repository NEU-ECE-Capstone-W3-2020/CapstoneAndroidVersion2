package com.example.capstoneandroidversion2.ble

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.capstoneandroidversion2.bus.BleServiceBus
import com.example.capstoneandroidversion2.bus.BusHolder
import com.example.capstoneandroidversion2.bus.FragmentToBleBus
import com.example.capstoneandroidversion2.ui.fragment.READ_CHAR
import com.example.capstoneandroidversion2.ui.fragment.SERVICE_UUID
import com.example.capstoneandroidversion2.ui.fragment.WRITE_CHAR
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

/**
 * Working bluetooth background service we'll use to maintain a connection to our device
 */
class BleService : Service() {
    private lateinit var bleManager: BleManager
    private val bus: Bus = BusHolder.bus
    private lateinit var bleConnectionManager: BleConnectionManager
    private val mBinder = LocalBinder()
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    inner class LocalBinder : Binder() {
        val service: BleService
            get() = this@BleService
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
        bleConnectionManager = BleConnectionManager(
            "Capstone Beacon",
            bluetoothAdapter
        ).apply {
            startBleScan()
            setResultCallback { result ->
                bus.post(
                    BleServiceBus(
                        isDeviceFound = true,
                        device = result
                    )
                )
                // WORKING CONNECTION
                bleManager = BleManager(applicationContext)
                bleManager
                    .connect(result.device)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .enqueue()
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
        event.shouldRead?.let {
            //TODO: read the currently advertised value back to the user
        }
        event.shouldWrite?.let {
            //TODO: Maybe add a way to write values to the
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
            .done {
                bleManager.close()
                logger("CLOSED")
            }.enqueue()
        logger("DESTROYED")
    }
}
