package hr.fer.trackmyroute.api

import hr.fer.trackmyroute.data.model.Route

object RoutesRepository {
    var routeList: MutableList<Route> = mutableListOf()
}