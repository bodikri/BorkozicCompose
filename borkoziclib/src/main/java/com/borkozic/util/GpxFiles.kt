package com.borkozic.util

import com.borkozic.data.Route
import com.borkozic.data.RoutePoint
import com.borkozic.data.Track
import com.borkozic.data.TrackPoint
import com.borkozic.data.Waypoint
import java.io.File
import java.io.IOException

object GpxFiles {

    @Throws(IOException::class)
    fun loadWaypointsFromFile(file: File): ArrayList<Waypoint> {
        val waypoints = ArrayList<Waypoint>()

        // TODO: Имплементирай GPX парсване

        return waypoints
    }

    @Throws(IOException::class)
    fun loadTracksFromFile(file: File): ArrayList<Track> {
        val tracks = ArrayList<Track>()

        // TODO: Имплементирай GPX парсване за tracks

        return tracks
    }

    @Throws(IOException::class)
    fun loadRoutesFromFile(file: File): ArrayList<Route> {
        val routes = ArrayList<Route>()

        // TODO: Имплементирай GPX парсване за routes

        return routes
    }
}