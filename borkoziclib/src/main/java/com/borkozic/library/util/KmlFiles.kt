package com.borkozic.library.util

import com.borkozic.library.data.Route
import com.borkozic.library.data.Track
import com.borkozic.library.data.Waypoint
import java.io.File
import java.io.IOException

object KmlFiles {

    @Throws(IOException::class)
    fun loadRoutesFromFile(file: File): ArrayList<Route> {
        val routes = ArrayList<Route>()

        // TODO: Имплементирай KML парсване
        // Ще добавим XML парсване по-късно

        return routes
    }

    @Throws(IOException::class)
    fun loadWaypointsFromFile(file: File): ArrayList<Waypoint> {
        val waypoints = ArrayList<Waypoint>()

        // TODO: Имплементирай KML парсване за waypoints

        return waypoints
    }

    @Throws(IOException::class)
    fun loadTracksFromFile(file: File): ArrayList<Track> {
        val tracks = ArrayList<Track>()

        // TODO: Имплементирай KML парсване за tracks

        return tracks
    }
}