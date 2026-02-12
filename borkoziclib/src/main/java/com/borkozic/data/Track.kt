package com.borkozic.data

import java.util.ArrayList

data class TrackPoint(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var speed: Double = 0.0,
    var bearing: Double = 0.0,
    var accuracy: Double = 0.0,
    var time: Long = 0,
    var continous: Boolean = false
)

class Track {
    var name: String = ""
    var description: String = ""
    var points: ArrayList<TrackPoint> = ArrayList()
    var show: Boolean = true
    var color: Int = 0

    fun addPoint(
        continous: Boolean,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        speed: Double,
        bearing: Double,
        accuracy: Double,
        time: Long
    ) {
        points.add(TrackPoint(latitude, longitude, altitude, speed, bearing, accuracy, time, continous))
    }
}