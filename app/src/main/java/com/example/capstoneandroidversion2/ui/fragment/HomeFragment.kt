package com.example.capstoneandroidversion2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.ble.*
import com.example.capstoneandroidversion2.bus.BleServiceBus
import com.example.capstoneandroidversion2.bus.BusHolder
import com.example.capstoneandroidversion2.bus.FragmentToBleBus
import com.squareup.otto.Subscribe

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val connectButton: Button = root.findViewById(R.id.home_button)
        connectButton.setOnClickListener {
            requireActivity().startService(
                Intent(
                    requireActivity(),
                    BleService::class.java
                )
            )
        }
        val disconnectButton: Button = root.findViewById(R.id.disconnect_service_button)
        disconnectButton.setOnClickListener {
            val stop = requireActivity().stopService(
                Intent(
                    requireActivity(),
                    BleService::class.java
                )
            )
            Log.e("TAG", stop.toString())
        }
        val readButton: Button = root.findViewById(R.id.read_button)
        readButton.setOnClickListener {
            BusHolder.bus.post(FragmentToBleBus(shouldRead = 1))
        }
        val toWrite = "NewString"
        val writeButton: Button = root.findViewById(R.id.write_button)
        writeButton.setOnClickListener {
            BusHolder.bus.post(FragmentToBleBus(shouldWrite = toWrite))
        }
        BusHolder.bus.register(this)
        return root
    }

    /**
     * Bus subscriber function to receive updates from our service
     */
    @Subscribe
    fun receiveEvent(event: BleServiceBus) {
        event.isDeviceFound?.let {
            System.out.println("DEVICE FOUND")
        }
        event.device?.let {
            System.out.println("SCAN RESULT ${it.device}")
        }
        event.currentReadValue?.let {
            //TODO: make this spit the value into our repo
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    it.removeNewLine(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

private fun String.removeNewLine(): String =
    this.removeSuffix("\n")

