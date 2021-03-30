package com.example.capstoneandroidversion2.ble

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.capstoneandroidversion2.ui.fragment.READ_CHAR
import com.polidea.rxandroidble2.RxBleClient
import java.nio.charset.Charset

const val MAC_ADDR = "D7:3D:77:BF:DB:B3"

/**
 * TODO: this service uses the RX bluetooth library to simplify transactions, however is not currently functional
 */
class BleService : Service() {

    private var rxBleClient: RxBleClient? = null
    private val mBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: BleService
            get() = this@BleService
    }

    override fun onBind(intent: Intent?): IBinder? =
        mBinder

    override fun onCreate() {
        logger("Service Started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger("OnStartCommand")
        rxBleClient = RxBleClient.create(this).apply {
            val device = getBleDevice(MAC_ADDR)
            // READING FROM A FOUND DEVICE
            device.establishConnection(false)
                .flatMapSingle { it.readCharacteristic(READ_CHAR) }
                .subscribe({ charVal ->
                    logger(charVal.toString(Charset.defaultCharset()))
                }, {
                    logger(it.localizedMessage ?: it.toString())
                })
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
}