package com.example.driveclassifier.models

data class LocationModel(
    val lang: Double,
    val lat: Double,
    val roadUse: String,
    val speed: Double,
    val speedLimit: Double,
    val speedDiff: Double
)
