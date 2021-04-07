package com.example.capstoneandroidversion2.ble

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.model.NotificationMessage
import com.example.capstoneandroidversion2.ui.*

/**
 * Working bluetooth background service we'll use to maintain a connection to our device
 */
class BleService : Service() {

    private val CHANNEL_ID = "CHANNEL_ID"
    private val CHANNEL_NAME = "CHANNEL_NAME"
    private val CHANNEL_DESCRIPTION = "CHANNEL_DESCRIPTION"

    private var bleManager: BleManager? = null
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
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger("OnStartCommand")
        // first scan for devices
        bleConnectionManager = BleConnectionManager(
            resources.getString(R.string.beacon_name),
            bluetoothAdapter
        ).apply {
            startBleScan()
            setResultCallback { result ->
                // WORKING CONNECTION
                bleManager = BleManager(applicationContext, resources) { msg ->
                    broadcastString(msg)
                    showNotification(msg)
                }.apply {
                    this.connect(result.device)
                        .retry(3, 100)
                        .useAutoConnect(false)
                        .enqueue()
                }
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

    override fun onDestroy() {
        super.onDestroy()
        bleManager?.let { manager ->
            manager.disconnect()
                .done {
                    manager.close()
                    logger("CLOSED")
                }
                .enqueue()
            logger("DESTROYED")
        }
    }

    private fun showNotification(message: NotificationMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val notification: Notification
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        // Configure the notification channel
        notificationChannel.description = CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(notificationChannel)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        // creating notification for the user
        notification = builder.setContentTitle(message.subject)
            .setContentText(message.body)
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(0, notification)
    }

    private fun broadcastString(msg: NotificationMessage) {
        val intent = Intent(BROADCAST_GET_STRING).apply {
            putExtra(BLUETOOTH_MESSAGE_KEY, msg)
            sendBroadcast(this)
        }
    }
}
