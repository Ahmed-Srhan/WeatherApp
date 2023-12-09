package com.example.weatherapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.adapter.WeatherToday
import com.example.weatherapp.databinding.ActivityWeatherBinding
import com.example.weatherapp.modal.WeatherList
import com.example.weatherapp.util.Resource
import com.example.weatherapp.util.SharedPrefs
import com.example.weatherapp.util.Utils.Companion.PERMISSION_REQUEST_CODE
import com.example.weatherapp.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPrefs
    private lateinit var binding: ActivityWeatherBinding
    private val viewModel by viewModels<WeatherViewModel>()
    lateinit var adapter: WeatherToday

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weather)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        adapter = WeatherToday()
        sharedPrefs.clearCityValue()

        // Check for location permissions
        if (checkLocationPermissions()) {
            // Permissions are granted, proceed to get the current location
            getCurrentLocation()
        } else {
            // Request location permissions
            requestLocationPermissions()
        }


        lifecycleScope.launch {
            viewModel.todayWeather.collect {
                showProgressBar()
                when (it) {
                    is Resource.Error -> {
                        hideProgressBar()
                        Toast.makeText(
                            applicationContext,
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Resource.Loading -> {
                        showProgressBar()
                    }

                    is Resource.Success -> {
                        hideProgressBar()
                        updateWeatherData(it.data!!)
                    }

                    else -> Unit
                }
            }

        }


        lifecycleScope.launch {
            viewModel.closeToExactlySameWeatherData.collect { result ->
                showProgressBar()
                when (result) {
                    is Resource.Error -> {
                        hideProgressBar()
                        Toast.makeText(
                            applicationContext,
                            result.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    is Resource.Loading -> {
                        showProgressBar()
                    }

                    is Resource.Success -> {
                        hideProgressBar()
                        result.data?.let {
                            val temperatureCelsius = it.main?.temp?.minus(273.15) ?: 0.0
                            val temperatureFormatted = String.format("%.2f", temperatureCelsius)

                            for (i in it.weather) {
                                binding.descMain.text = i.description
                            }

                            binding.tempMain.text = "$temperatureFormatted°"
                            binding.humidityMain.text = it.main!!.humidity.toString()
                            binding.windSpeed.text = it.wind?.speed.toString()


                            val inputFormat =
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val date = inputFormat.parse(it.dtTxt!!)
                            val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
                            val dateAndDayName = outputFormat.format(date!!)

                            binding.dateDayMain.text = dateAndDayName

                            binding.chanceofrain.text = "${it.pop.toString()}%"


                            for (i in it.weather) {
                                setWeatherIcon(i.icon)

                            }
                        }
                    }

                    else -> Unit
                }


            }
        }


        val searchEditText =
            binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white))


        binding.next5Days.setOnClickListener {
            startActivity(Intent(this, ForeCastActivity::class.java))
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    // Update the city value in shared preferences
                    sharedPrefs.setValueOrNull("city", query)

                    // Fetch weather data for the submitted query
                    viewModel.getWeather(query)

                    // Clear and collapse the SearchView
                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    binding.searchView.isIconified = true
                } else {
                    // Display a message to the user indicating that the query is empty
                    Toast.makeText(
                        applicationContext,
                        "Please enter a city name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    private fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    // Function to request location permissions
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    // Handle the permission request result
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions granted, get the current location
                getCurrentLocation()
            } else {
                // Permissions denied, handle accordingly
                Toast.makeText(
                    this,
                    "Location permission denied. Some features may not work properly.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Function to get the current location
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location: Location? =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                // Use the latitude and longitude values as needed
                // ...

                sharedPrefs.setValue("lon", longitude.toString())
                sharedPrefs.setValue("lat", latitude.toString())


                // Example: Display latitude and longitude in logs


                Toast.makeText(
                    this,
                    "Latitude: $latitude, Longitude: $longitude",
                    Toast.LENGTH_SHORT
                ).show()


                Log.d("Current Location", "Latitude: $latitude, Longitude: $longitude")

                // Reverse geocode the location to get address information
                reverseGeocodeLocation(latitude, longitude)
            } else {
                // Location is null, handle accordingly
                Toast.makeText(
                    this,
                    "Unable to fetch current location. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()

            }
        } else {
            // Location permission not granted, handle accordingly
            Toast.makeText(
                this,
                "Location permission not granted. Some features may not work properly.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Function to reverse geocode the location and get address information
    private fun reverseGeocodeLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                // Use the addressLine as needed
                // ...
                // Example: Display address in logs
                Log.d("Current Address", addressLine)
            } else {
                // No address found, handle accordingly
                Toast.makeText(
                    this,
                    "Unable to fetch address. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle geocoding failure, e.g., show an error message
            Toast.makeText(
                this,
                "Geocoding failed. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateWeatherData(weatherList: List<WeatherList>) {
        adapter.differ.submitList(weatherList)
        binding.forecastRecyclerView.adapter = adapter
    }

    // setting the icon
    private fun setWeatherIcon(iconCode: String?) {
        when (iconCode) {
            "01d" -> binding.imageMain.setImageResource(R.drawable.oned)
            "01n" -> binding.imageMain.setImageResource(R.drawable.onen)
            "02d" -> binding.imageMain.setImageResource(R.drawable.twod)
            "02n" -> binding.imageMain.setImageResource(R.drawable.twon)
            "03d", "03n" -> binding.imageMain.setImageResource(R.drawable.threedn)
            "04d", "04n" -> binding.imageMain.setImageResource(R.drawable.fourdn)
            "09d", "09n" -> binding.imageMain.setImageResource(R.drawable.ninedn)
            "10d" -> binding.imageMain.setImageResource(R.drawable.tend)
            "10n" -> binding.imageMain.setImageResource(R.drawable.tenn)
            "11d", "11n" -> binding.imageMain.setImageResource(R.drawable.elevend)
            "13d", "13n" -> binding.imageMain.setImageResource(R.drawable.thirteend)
            "50d", "50n" -> binding.imageMain.setImageResource(R.drawable.fiftydn)
            else -> Unit
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }


}








