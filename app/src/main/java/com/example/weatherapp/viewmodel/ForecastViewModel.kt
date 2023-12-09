package com.example.weatherapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.modal.WeatherList
import com.example.weatherapp.repository.ForecastRepository
import com.example.weatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val forecastRepository: ForecastRepository
) : ViewModel() {

    private val _forecastWeather = MutableStateFlow<Resource<List<WeatherList>>?>(null)
    val forecastWeather = _forecastWeather.asStateFlow()


    @RequiresApi(Build.VERSION_CODES.O)
    fun getForecastUpcoming(city: String? = null) = viewModelScope.launch(Dispatchers.IO) {
        forecastRepository.getForecastUpcoming(city).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _forecastWeather.value = Resource.Success(result.data!!, result.cityName)

                }

                is Resource.Error -> {
                    _forecastWeather.value = Resource.Error()

                }

                is Resource.Loading -> {
                    _forecastWeather.value = Resource.Loading()
                }
            }
        }


    }

}