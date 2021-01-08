package hr.fer.trackmyroute.viewmodel

import java.time.Duration
import java.time.LocalTime

object durationRepository {

    fun fetchData(dataId: LocalTime): LocalTime {
        Thread.sleep(1000)
        return dataId
    }

}