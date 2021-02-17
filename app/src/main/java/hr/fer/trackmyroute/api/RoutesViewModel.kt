package hr.fer.trackmyroute.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hr.fer.trackmyroute.data.model.Route
import java.lang.IndexOutOfBoundsException

class RoutesViewModel : ViewModel() {
    var routeList = MutableLiveData<List<Route>>()

    fun getRoutesRepository() {
        routeList.value = RoutesRepository.routeList
    }

    fun clearRoutesRepository() {
        routeList = MutableLiveData<List<Route>>()
        RoutesRepository.routeList.clear();
    }

    fun saveRouteToRepository(route: Route) {
        RoutesRepository.routeList.add(route)
        RoutesRepository.routeList[0] = RoutesRepository.routeList[0]
    }

    fun updateRouteInRepository(position: Int, route: Route) {
        RoutesRepository.routeList[position] = route
    }

    fun removeRouteFromRepository(position: Int) {
        if (position < RoutesRepository.routeList.size)
            RoutesRepository.routeList.removeAt(position)
    }

    fun getRouteFromRepository(position: Int): Route {
        if (position < RoutesRepository.routeList.size)
            return RoutesRepository.routeList[position]
        else throw IndexOutOfBoundsException()
    }
}