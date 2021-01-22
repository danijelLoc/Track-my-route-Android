package hr.fer.trackmyroute.data.model

import java.time.LocalTime
import java.io.Serializable

data class Location(
    var latitude: Double,
    var longitude: Double,
    //var moment: String = "",
    var id: Long = -1,
    var route: Route = Route()
) : Serializable