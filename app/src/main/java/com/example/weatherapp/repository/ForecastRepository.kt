package com.example.weatherapp.repository

import com.example.weatherapp.modal.WeatherList
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ForecastRepository {
    fun getForecastUpcoming(city: String? = null): Flow<Resource<List<WeatherList>>>

}