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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneandroidversion2.R
import java.util.*

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

val SERVICE_UUID: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e".toUpperCase())
val READ_CHAR: UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e".toUpperCase())
val WRITE_CHAR: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e".toUpperCase())

class BluetoothFragment() : Fragment() {

    private lateinit var pairButton: Button
    private lateinit var dashboardViewModel: BluetoothViewModel
    private val logTag: String = "BluetoothFragment"
    private lateinit var connectButton: Button


    val isLocationPermissionGranted
        get() = requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION).also {
            pairButton.visibility = if (it) VISIBLE else GONE
        }

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
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

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
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
                    pairButton.visibility = VISIBLE
                }
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
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

    override fun onResume() {
        super.onResume()
        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        }
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(BluetoothViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bluetooth, container, false)
        val nameInput: EditText = root.findViewById(R.id.device_name_edittext)
        val pairedLayout: View = root.findViewById(R.id.paired_layout)
        pairButton = root.findViewById(R.id.pair_button)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            //  textView.text = it
        })
        dashboardViewModel.isPaired.observe(viewLifecycleOwner, {
            pairedLayout.visibility = if (it) VISIBLE else GONE
        })
        pairButton.setOnClickListener {
            dashboardViewModel.pairDevice(
                nameInput.text.toString(), bluetoothAdapter, SERVICE_UUID,
                READ_CHAR, WRITE_CHAR, requireContext()
            )
        }
        connectButton = root.findViewById(R.id.connect_device_button)
        connectButton.setOnClickListener {
            dashboardViewModel.connectToFoundDevice()
        }
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        dashboardViewModel.unPairDevice()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel.viewState.observe(viewLifecycleOwner, { handleViewState(it) })
    }

    private fun handleViewState(state: BluetoothViewState) {
        state.scanResult?.let {
            enableConnecting(it.device.name)
        }
    }

    private fun enableConnecting(name: String) {
        val connectDeviceTextview = requireView().findViewById<TextView>(R.id.found_device_textview)
        val connectDeviceView = requireView().findViewById<View>(R.id.found_device_layout)
        connectDeviceTextview.text = "Found Device:" + name
        connectDeviceView.visibility = VISIBLE
    }


}
