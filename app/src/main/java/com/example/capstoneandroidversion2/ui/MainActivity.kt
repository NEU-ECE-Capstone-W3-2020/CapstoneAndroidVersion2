package com.example.capstoneandroidversion2.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            intent?.extras?.let { bundle ->
                (bundle.getSerializable(BLUETOOTH_MESSAGE_KEY) as NotificationMessage).let {
                    viewModel.postReadValue(it.body)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_bluetooth, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_GET_STRING))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
    }
}
