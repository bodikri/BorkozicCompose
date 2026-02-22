package com.borkozic.library.data

import java.util.ArrayList

data class RoutePoint(
    var name: String = "",
    var description: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var proximity: Double = 0.0,
    var color: Int = 0,
    var icon: String = ""
)

class Route {
    var name: String = ""
    var description: String = ""
    var points: ArrayList<RoutePoint> = ArrayList()
    var show: Boolean = true
    var color: Int = 0
    var width: Int = 2

    fun addPoint(point: RoutePoint) {
        points.add(point)
    }
}