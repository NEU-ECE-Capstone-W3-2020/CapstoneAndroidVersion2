package com.example.capstoneandroidversion2.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.model.NotificationMessage

const val BLUETOOTH_MESSAGE_KEY = "MESSAGE"
const val BROADCAST_GET_STRING = "STRING_BROADCAST"

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            intent?.extras?.let { bundle ->
                // for the read value
                (bundle.getSerializable(BLUETOOTH_MESSAGE_KEY) as NotificationMessage)?.let {
                    viewModel.postReadValue(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_GET_STRING))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
