package hr.fer.trackmyroute.viewmodel

object distanceRepository {
    fun fetchData(dataId: Double): Double {
        Thread.sleep(5000)
        return dataId
    }
}