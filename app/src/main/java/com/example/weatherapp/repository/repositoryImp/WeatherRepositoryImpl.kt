package com.example.weatherapp.repository.repositoryImp

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weatherapp.modal.WeatherList
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.service.WeatherApiService
import com.example.weatherapp.util.Resource
import com.example.weatherapp.util.SharedPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val sharedPrefs: SharedPrefs
) : WeatherRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getWeather(city: String?): Flow<Resource<List<WeatherList>>> = flow {
        emit(Resource.Loading())

        try {
            val todayWeatherList = mutableListOf<WeatherList>()

            val currentDateTime = LocalDateTime.now()
            val currentDateO = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val lat = sharedPrefs.getValue("lat").toString()
            val lon = sharedPrefs.getValue("lon").toString()

            val response = if (city != null) {
                weatherApiService.getWeatherByCity(city)
            } else {
                weatherApiService.getCurrentWeather(lat, lon)
            }

            if (response.isSuccessful) {
                val weatherList = response.body()?.weatherList

                val cityName = response.body()?.city?.name

                weatherList?.forEach { weather ->
                    if (weather.dtTxt?.split("\\s".toRegex())?.contains(currentDateO) == true) {
                        todayWeatherList.add(weather)
                    }
                }

                emit(Resource.Success(todayWeatherList, cityName))
            } else {
                emit(Resource.Error())
            }
        } catch (e: Exception) {
            emit(Resource.Error())
        }
    }.flowOn(Dispatchers.IO)
}
