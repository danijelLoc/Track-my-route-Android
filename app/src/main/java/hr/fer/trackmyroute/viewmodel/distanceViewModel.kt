package hr.fer.trackmyroute.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class distanceViewModel: ViewModel() {

    val resultOfDataFetch = MutableLiveData<String>()
    var stopFlag = false
    var startFlag = false
    var distance: Double = 0.0

    fun onStop()
    {
        viewModelScope.apply {
            stopFlag = true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchDataFromRepository() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(!startFlag)
                {
                    startFlag = true
                    stopFlag = false
                    var i: Double = 0.0
                    var s: String = ""

                    while(true)
                    {
                        if (stopFlag) {
                            stopFlag = false
                            break
                        }



                        if(distance < 1)
                        {
                            s = String.format("%7.0f m", distance * 1000)
                        }
                        else
                        {
                            s = String.format("%7.2f km", distance)
                        }

                        withContext(Dispatchers.Main) {
                            resultOfDataFetch.value = s
                        }
                        distance = calculateDistance(45.817957692522924, 16.068609569335337, 45.81790392046314, 16.068616135669703)
                        distance += i
                        i += 0.001
                        distance = distanceRepository.fetchData(distance)
                        //distance += 0.001
                    }
                }
            }
        }
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double
    {
        var distance: Double
        val radius : Int = 6371
        var dLat : Double = degreeToRadian(lat2-lat1)
        var dLon : Double = degreeToRadian(lon2-lon1)

        var a: Double = sin(dLat/2) * sin(dLat/2) +
                cos(degreeToRadian(lat1)) * cos(degreeToRadian(lat2)) *
                sin(dLon/2) * sin(dLon/2)

        var c: Double = 2 * atan2(sqrt(a), sqrt(1-a))

        distance = radius * c

        return distance
    }

    fun degreeToRadian(deg: Double): Double
    {
        return deg * (Math.PI / 180)
    }

}