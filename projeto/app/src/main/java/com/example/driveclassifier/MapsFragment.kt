package com.example.driveclassifier

import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.driveclassifier.models.LocationModel
import com.example.driveclassifier.models.TripModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsFragment : Fragment() {
    private lateinit var trip: TripModel
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        if (::trip.isInitialized) {
            val location = mutableListOf<LatLng>()

            var previousLocation: LocationModel? = null
            var previousColor: Int? = null
            var currentPolylineOptions: PolylineOptions? = null

            trip.locations.forEach { locationModel: LocationModel ->
                val latLng = LatLng(locationModel.lat, locationModel.lang)
                val speedDiff = locationModel.speedDiff

                when {
                    speedDiff == 0.0 -> {
                        if (previousColor != Color.parseColor("#008000")) {
                            // Create a new polyline for the green segment
                            currentPolylineOptions = PolylineOptions().clickable(true)
                                .color(Color.parseColor("#008000"))
                            previousColor = Color.parseColor("#008000")
                        }
                    }
                    speedDiff in 0.1..10.0 -> {
                        if (previousColor != Color.parseColor("#FAFA33")) {
                            // Create a new polyline for the yellow segment
                            currentPolylineOptions = PolylineOptions().clickable(true)
                                .color(Color.parseColor("#FAFA33"))
                            previousColor = Color.parseColor("#FAFA33")
                        }
                    }
                    speedDiff in 11.0..20.0 -> {
                        if (previousColor != Color.parseColor("#FFA500")) {
                            // Create a new polyline for the orange segment
                            currentPolylineOptions = PolylineOptions().clickable(true)
                                .color(Color.parseColor("#FFA500"))
                            previousColor = Color.parseColor("#FFA500")
                        }
                    }
                    else -> {
                        if (previousColor != Color.RED) {
                            // Create a new polyline for the red segment
                            currentPolylineOptions = PolylineOptions().clickable(true)
                                .color(Color.RED)
                            previousColor = Color.RED
                        }
                    }
                }

                currentPolylineOptions?.add(latLng)
                location.add(latLng)
                previousLocation = locationModel

                // Add the completed polyline to the map
                currentPolylineOptions?.let {
                    googleMap.addPolyline(it)
                }
            }

            // Position the map's camera near the first location in the trip
            val firstLocation = trip.locations.firstOrNull()
            if (firstLocation != null) {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            firstLocation.lat,
                            firstLocation.lang
                        ), 12f
                    )
                )
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        val closeIcon = view.findViewById<ImageView>(R.id.closeIcon)
        closeIcon.setOnClickListener { onCloseIconClicked() }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        trip = (arguments?.getSerializable("trip") as? TripModel)!!
        mapFragment?.getMapAsync(callback)
    }


    private fun onCloseIconClicked() {
        parentFragmentManager.beginTransaction().remove(this).commit()
    }
}
