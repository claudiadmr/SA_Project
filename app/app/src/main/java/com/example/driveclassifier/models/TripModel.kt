package com.example.driveclassifier.models

import java.io.Serializable

data class TripModel(
    val duration: Int,
    val endDate: String,
    val locations: List<LocationModel>,
    val nameTrip: String,
    val startDate: String,
    val aggressiveDriving: Double,
    val moderateDriving: Double,
    val normalDriving: Double,
) : Serializable
