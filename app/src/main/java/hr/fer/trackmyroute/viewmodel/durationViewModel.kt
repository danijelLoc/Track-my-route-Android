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
import hr.fer.trackmyroute.viewmodel.distanceViewModel


class durationViewModel : ViewModel() {

    val resultOfDataFetch = MutableLiveData<String>()
    var stopFlag = false
    var startFlag = false

    @RequiresApi(Build.VERSION_CODES.O)
    var startTime: LocalTime = LocalTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    var currentTime: LocalTime = LocalTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    var duration: LocalTime = LocalTime.now()
    var durationString: String = ""
    var durationInHours: Double = 0.0
    var durationInMinutes: Double = 0.0
    var durationInSeconds: Double = 0.0

    fun onStop() {
        viewModelScope.apply {
            stopFlag = true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchDataFromRepository() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (!startFlag) {
                    startTime = LocalTime.now()
                    startFlag = true
                    var fetchedResult: LocalTime
                    stopFlag = false

                    while (true) {
                        if (stopFlag) {
                            stopFlag = false
                            currentTime = startTime
                            break
                        }

                        currentTime = LocalTime.now()
                        duration = currentTime.minusHours(startTime.hour.toLong())
                        duration = duration.minusMinutes(startTime.minute.toLong())
                        duration = duration.minusSeconds(startTime.second.toLong())
                        durationInHours = duration.hour.toDouble()
                        durationInHours += duration.minute.toDouble() / 60.0
                        durationInHours += duration.second.toDouble() / 3600.0
                        durationInMinutes = duration.hour.toDouble() * 60
                        durationInMinutes += duration.minute.toDouble()
                        durationInMinutes += duration.second.toDouble() / 60.0
                        durationInSeconds = duration.hour.toDouble() * 3600.0
                        durationInSeconds += duration.minute.toDouble() * 60
                        durationInSeconds += duration.second.toDouble()
                        val builder = StringBuilder()
                        if (duration.hour<10) {
                            builder.append("0")
                        }
                        builder.append(duration.hour.toString())
                        builder.append(":")
                        if (duration.minute<10) {
                            builder.append("0")
                        }
                        builder.append(duration.minute.toString())
                        builder.append(":")
                        if (duration.second<10) {
                            builder.append("0")
                        }
                        builder.append(duration.second.toString())
                        durationString = builder.toString()
                        withContext(Dispatchers.Main) {
                            /*resultOfDataFetch.value =
                                duration.format(DateTimeFormatter.ofPattern("HH:mm:ss"))*/
                            resultOfDataFetch.value = durationString
                        }
                        duration = durationRepository.fetchData(duration)
                    }
                }
            }
        }
    }
}