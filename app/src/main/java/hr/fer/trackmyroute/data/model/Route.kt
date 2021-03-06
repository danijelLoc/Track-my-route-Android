package hr.fer.trackmyroute.data.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.*


data class Route(
    var id: Long = -1,
    var user: User = User(),
    var name: String = "",
    // problem with date format when converting back to json (using string cause of that)
    var date: String = "",
    // now in seconds
    var duration: Double = -1.0,
    // still km/h
    var speed: Double = -1.0,
    var distance: Double = -1.0
) : Serializable
