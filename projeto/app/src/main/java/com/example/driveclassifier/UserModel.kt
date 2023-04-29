package com.example.driveclassifier

data class UserModel(
    var id: Int ?=null,
    var lang: Double ? = 0.0,
    var lat: Double ? = 0.0,
    var speed: Float ? = 0f,
    var date: String ? = null
)
