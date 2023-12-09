package com.example.weatherapp.viewmodel


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.modal.WeatherList
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {


    private val _todayWeather = MutableStateFlow<Resource<List<WeatherList>>?>(null)
    val todayWeather = _todayWeather.asStateFlow()


    private val _closeToExactlySameWeatherData = MutableStateFlow<Resource<WeatherList>?>(null)
    val closeToExactlySameWeatherData = _closeToExactlySameWeatherData.asStateFlow()

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    init {
        getWeather()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeather(city: String? = null) = viewModelScope.launch {
        weatherRepository.getWeather(city).collect { result ->
            when (result) {
                is Resource.Success -> {
                    handleSuccessResult(result.data, result.cityName)
                }

                is Resource.Error -> {
                    handleErrorResult(result.message)
                }

                is Resource.Loading -> {
                    handleLoadingResult()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun findClosestWeather(weatherList: List<WeatherList>): WeatherList? {
        val systemTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        var closestWeather: WeatherList? = null
        var minTimeDifference = Int.MAX_VALUE

        for (weather in weatherList) {
            val weatherTime = weather.dtTxt!!.substring(11, 16)
            val timeDifference = abs(timeToMinutes(weatherTime) - timeToMinutes(systemTime))

            if (timeDifference < minTimeDifference) {
                minTimeDifference = timeDifference
                closestWeather = weather
            }
        }

        return closestWeather
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun handleSuccessResult(weatherList: List<WeatherList>?, city: String?) {
        _cityName.value = city
        _todayWeather.value = Resource.Success(weatherList!!, city)
        _closeToExactlySameWeatherData.value =
            Resource.Success(findClosestWeather(weatherList)!!, city)
    }

    private fun handleErrorResult(errorMessage: String?) {
        val message = errorMessage ?: "Unknown error"
        _todayWeather.value = Resource.Error()
        _closeToExactlySameWeatherData.value = Resource.Error()
    }

    private fun handleLoadingResult() {
        _todayWeather.value = Resource.Loading()
        _closeToExactlySameWeatherData.value = Resource.Loading()
    }
}