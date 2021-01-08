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

class durationViewModel: ViewModel() {

    val resultOfDataFetch = MutableLiveData<String>()
    var stopFlag = false
    var startFlag = false
    @RequiresApi(Build.VERSION_CODES.O)
    var startTime: LocalTime = LocalTime.now()
    @RequiresApi(Build.VERSION_CODES.O)
    var currentTime: LocalTime = LocalTime.now()
    @RequiresApi(Build.VERSION_CODES.O)
    var duration: LocalTime = LocalTime.now()
    var durationInHours: Double = 0.0

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
                    startTime = LocalTime.now()
                    startFlag = true
                    var fetchedResult: LocalTime
                    stopFlag = false

                    while(true)
                    {
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
                        withContext(Dispatchers.Main) {
                            resultOfDataFetch.value = duration.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        }

                        duration = durationRepository.fetchData(duration)
                    }
                }
            }
        }
    }
}