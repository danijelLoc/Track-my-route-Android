package hr.fer.trackmyroute.data.model

data class RouteLocation(
    var id: Long = -1,
    var route: Route = Route(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)