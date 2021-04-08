package com.example.capstoneandroidversion2.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.model.NotificationMessage
import com.example.capstoneandroidversion2.ui.MAP_DTO_KEY
import com.example.capstoneandroidversion2.ui.MainViewModel
import com.example.capstoneandroidversion2.ui.MapActivity
import com.example.capstoneandroidversion2.ui.TagAdapter
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerview: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,


        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerview = view.findViewById(R.id.history_recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(requireContext())
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.readPosts?.let { list ->
                updateAdapter(list)
            }


        })
        // even if we have nothing (REMOVE AFTER TESTING)
        updateAdapter(
            mutableListOf(
                NotificationMessage(
                    body = getString(R.string.tag_4),
                    subject = "New Tag Discovered",
                    timestamp = SimpleDateFormat("hh:mm a MMMM dd, YYYY").format(Date()),
                    lat = null,
                    long = null
                )
            )
        )
    }

    private fun updateAdapter(list: List<NotificationMessage>) {
        recyclerview.adapter = TagAdapter(list.reversed()) {
            val intent = Intent(requireContext(), MapActivity::class.java).apply {
                putExtra(MAP_DTO_KEY, it)
            }
            startActivity(intent)
        }
    }
}
