package hr.fer.trackmyroute.data.model

data class RouteLocationsResponse (val error:Boolean, val message:String, val routeLocations:List<hr.fer.trackmyroute.data.model.RouteLocation>)