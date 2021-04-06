package com.example.capstoneandroidversion2.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.bus.BusHolder
import com.example.capstoneandroidversion2.ui.MainViewModel
import com.example.capstoneandroidversion2.ui.TagAdapter

//TODO: refactor this for actual use or kill the screen
class HistoryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerview: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,


        savedInstanceState: Bundle?
    ): View? {
        BusHolder.bus.register(this)
        viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerview = view.findViewById(R.id.history_recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(requireContext())
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            viewState.readPosts?.let { list ->
                recyclerview.adapter = TagAdapter(list) {
                    //TODO: maybe read the message? I think accessibility settings might already do this
                }
            }
        }
    }
}
