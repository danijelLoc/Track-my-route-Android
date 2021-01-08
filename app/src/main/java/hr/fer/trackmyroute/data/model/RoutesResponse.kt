package hr.fer.trackmyroute.data.model

data class RoutesResponse (val error:Boolean, val message:String, val routes:List<hr.fer.trackmyroute.data.model.Route>)