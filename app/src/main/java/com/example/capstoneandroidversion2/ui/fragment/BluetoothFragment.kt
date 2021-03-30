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
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.ui.MainViewModel
import com.example.capstoneandroidversion2.ui.MainViewState
import java.util.*

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

val SERVICE_UUID: UUID =
    UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e".toUpperCase(Locale.ROOT))
val READ_CHAR: UUID =
    UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e".toUpperCase(Locale.ROOT))
val WRITE_CHAR: UUID =
    UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e".toUpperCase(Locale.ROOT))

/**
 * TODO: refactor this to work with our new service
 */
class BluetoothFragment() : Fragment() {

    private lateinit var pairButton: Button
    private lateinit var viewModel: MainViewModel
    private val logTag: String = "BluetoothFragment"
    private lateinit var connectButton: Button

    // permissions
    val isLocationPermissionGranted
        get() = requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION).also {
            pairButton.visibility = if (it) VISIBLE else GONE
        }

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

    private fun handleViewState(state: MainViewState) {
        state.scanResult?.let {
            showConnectingLayout(it.device.name)
        }
    }

    private fun showConnectingLayout(deviceName: String) {
        val connectDeviceTextview = requireView().findViewById<TextView>(R.id.found_device_textview)
        val connectDeviceView = requireView().findViewById<View>(R.id.found_device_layout)
        connectDeviceTextview.text = "Found Device: $deviceName"
        connectDeviceView.visibility = VISIBLE
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
                    pairButton.visibility = VISIBLE
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
        viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bluetooth, container, false)
        val nameInput: EditText = root.findViewById(R.id.device_name_edittext)
        val pairedLayout: View = root.findViewById(R.id.paired_layout)
        pairButton = root.findViewById(R.id.pair_button)
       /* viewModel.isPaired.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            pairedLayout.visibility = if (it) VISIBLE else GONE
        })*/
        pairButton.setOnClickListener {
            /*viewModel.pairDevice(
                nameInput.text.toString(), bluetoothAdapter, SERVICE_UUID,
                READ_CHAR, WRITE_CHAR, requireContext()
            )*/
        }
        connectButton = root.findViewById(R.id.connect_device_button)
        connectButton.setOnClickListener {
            /*viewModel.connectToFoundDevice()*/
        }
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
       /* viewModel.unPairDevice()*/
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewState.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { handleViewState(it) })
    }

    // ext functions
    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

}
