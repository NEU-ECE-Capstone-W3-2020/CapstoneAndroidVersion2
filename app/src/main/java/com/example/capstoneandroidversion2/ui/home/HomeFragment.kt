package com.example.capstoneandroidversion2.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.ble.*
import com.squareup.otto.Subscribe

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        val connectButton: Button = root.findViewById(R.id.home_button)
        connectButton.setOnClickListener {
            requireActivity().startService(
                Intent(
                    requireActivity(),
                    BleServiceOldSchool::class.java
                )
            )
        }
        val disconnectButton: Button = root.findViewById(R.id.disconnect_service_button)
        disconnectButton.setOnClickListener {
            val stop = requireActivity().stopService(
                Intent(
                    requireActivity(),
                    BleServiceOldSchool::class.java
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
            BusHolder.bus.post(FragmentToBleBus(scanResult = it))
        }
        event.currentReadValue?.let {
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


}
