package hr.fer.trackmyroute.data.model

import java.io.Serializable
import java.util.*

data class Route(
    var id: Long = -1,
    var user: User = User(),
    var name: String = "",
    // problem with date format when converting back to json (using string cause of that)
    var date: String = "",
    var duration: Long = -1,
    var speed: Double = -1.0,
    var distace: Double = -1.0
) : Serializable