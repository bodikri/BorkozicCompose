package com.borkozic.library.data

import java.util.Date

data class Waypoint(
    var name: String = "",
    var description: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var proximity: Double = 0.0,
    var color: Int = 0,
    var date: Date = Date(),
    var icon: String = ""
)