package com.example.capstoneandroidversion2.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.capstoneandroidversion2.R
import com.example.capstoneandroidversion2.model.NotificationMessage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

const val MAP_DTO_KEY = "mapdto"
const val DEFAULT_ZOOM = 15

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var timestampTextview: TextView
    private lateinit var titleTextview: TextView
    private lateinit var placeTextView: TextView

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        initViews()
    }

    private fun initViews() {
        titleTextview = findViewById(R.id.mapview_tag_textview)
        timestampTextview = findViewById(R.id.mapview_timestamp_textview)
        placeTextView = findViewById(R.id.mapview_place_textview)
    }

    override fun onMapReady(p0: GoogleMap?) {
        p0?.let {
            map = it
            getLocationFromIntent(it)
        }
    }

    private fun getLocationFromIntent(googleMap: GoogleMap) {
        (intent.getSerializableExtra(MAP_DTO_KEY) as NotificationMessage?)?.let {

            // setting the textviews
            titleTextview.text = it.body
            timestampTextview.text = it.timestamp
            //TODO: get the place (probably from maps sdk) and set this value dynamically
            placeTextView.text = it.subject
            // moving the map
            googleMap.apply {
                moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.lat,
                            it.long
                        ), DEFAULT_ZOOM.toFloat()
                    )
                )
                addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                it.lat,
                                it.long
                            )
                        )
                        .title(it.subject)
                )
            }
        }
    }
}
