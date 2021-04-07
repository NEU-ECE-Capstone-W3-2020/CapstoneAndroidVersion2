package com.example.capstoneandroidversion2.ui.fragment

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.ble.*
import com.example.capstoneandroidversion2.ui.MainViewModel

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private val logTag = "HomeFragment"


    // permissions
    val isLocationPermissionGranted
        get() = requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    // attaching bluetooth to the fragment (TODO fix this)
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        requireActivity().runOnUiThread {
            val builder = AlertDialog.Builder(requireContext())
                .setTitle("Location Permission required")
                .setMessage("Need location")
                .setPositiveButton("OK") { _, _ ->
                    requireActivity().requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            builder.show()
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    Log.i(logTag, "Permissions detected")
                    //pairButton.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        }
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    // ext functions
    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        connectButton = root.findViewById(R.id.home_button)
        disconnectButton = root.findViewById(R.id.disconnect_service_button)
        connectButton.setOnClickListener {
            requireActivity().startService(
                Intent(
                    requireActivity(),
                    BleService::class.java
                )
            )
        }
        disconnectButton.setOnClickListener {
            val stop = requireActivity().stopService(
                Intent(
                    requireActivity(),
                    BleService::class.java
                )
            )
            Log.e("TAG", stop.toString())
        }
        return root
    }
}

