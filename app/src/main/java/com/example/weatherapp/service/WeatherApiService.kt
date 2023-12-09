package com.example.weatherapp.service

import com.example.weatherapp.modal.ForeCast
import com.example.weatherapp.util.Utils.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast?")
    suspend fun getCurrentWeather(
        @Query("lat")
        lat: String,
        @Query("lon")
        lon: String,
        @Query("appid")
        appid: String = API_KEY

    ): Response<ForeCast>

    @GET("forecast?")
    suspend fun getWeatherByCity(
        @Query("q")
        city: String,
        @Query("appid")
        appid: String = API_KEY

    ): Response<ForeCast>


}