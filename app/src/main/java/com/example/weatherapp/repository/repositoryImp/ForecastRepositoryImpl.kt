package com.example.weatherapp.repository.repositoryImp

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weatherapp.modal.WeatherList
import com.example.weatherapp.repository.ForecastRepository
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

class ForecastRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val sharedPrefs: SharedPrefs
) : ForecastRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getForecastUpcoming(city: String?): Flow<Resource<List<WeatherList>>> = flow {
        emit(Resource.Loading())

        try {
            val forecastWeatherList = mutableListOf<WeatherList>()

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
                    if (!weather.dtTxt!!.split("\\s".toRegex()).contains(currentDateO)) {
                        if (weather.dtTxt!!.substring(11, 16) == "12:00") {
                            forecastWeatherList.add(weather)
                        }
                    }
                }

                emit(Resource.Success(forecastWeatherList, cityName))
            } else {
                emit(Resource.Error())
            }
        } catch (e: Exception) {
            emit(Resource.Error())
        }
    }.flowOn(Dispatchers.IO)
}
