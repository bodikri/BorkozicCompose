package com.borkozic.util

import com.borkozic.data.Route
import com.borkozic.data.RoutePoint
import com.borkozic.data.Waypoint
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

object OziExplorerFiles {

    @Throws(IOException::class)
    fun loadWaypointsFromFile(file: File, charset: String = "UTF-8"): ArrayList<Waypoint> {
        val waypoints = ArrayList<Waypoint>()

        BufferedReader(FileReader(file)).use { reader ->
            var line: String?
            var lineNumber = 0

            while (reader.readLine().also { line = it } != null) {
                lineNumber++

                // Пропускаме заглавните редове
                if (lineNumber == 1) continue // OziExplorer Waypoint File Version 1.1
                if (lineNumber == 2) continue // WGS 84
                if (lineNumber == 3) continue // Reserved

                val parts = line!!.split(",")
                if (parts.size >= 8) {
                    try {
                        val wpt = Waypoint(
                            name = parts[0],
                            latitude = parts[1].toDouble(),
                            longitude = parts[2].toDouble(),
                            date = java.util.Date(), // TODO: Parse date from parts[4]
                            icon = parts[6],
                            description = parts[8]
                        )
                        waypoints.add(wpt)
                    } catch (e: NumberFormatException) {
                        // Skip invalid lines
                    }
                }
            }
        }

        return waypoints
    }

    @Throws(IOException::class)
    fun loadRoutesFromFile(file: File, charset: String = "UTF-8"): ArrayList<Route> {
        val routes = ArrayList<Route>()

        // TODO: Имплементирай пълно парсване на .rt2/.rte формат
        // Засега връщаме празен списък

        return routes
    }
}