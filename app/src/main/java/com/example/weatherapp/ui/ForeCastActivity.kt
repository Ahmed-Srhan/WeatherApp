package com.example.weatherapp.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.adapter.ForeCastAdapter
import com.example.weatherapp.util.Resource
import com.example.weatherapp.util.SharedPrefs
import com.example.weatherapp.viewmodel.ForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForeCastActivity : AppCompatActivity() {
    private val viewModel: ForecastViewModel by viewModels()
    private lateinit var adapterForeCastAdapter: ForeCastAdapter
    private lateinit var rvForeCast: RecyclerView

    @Inject
    lateinit var sharedPrefs: SharedPrefs


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourday_forecast)

        adapterForeCastAdapter = ForeCastAdapter()
        rvForeCast = findViewById(R.id.rvForeCast)


        val city = sharedPrefs.getValueOrNull("city")

        if (city != null) {
            viewModel.getForecastUpcoming(city)

        } else {
            viewModel.getForecastUpcoming()

        }


        lifecycleScope.launch {
            viewModel.forecastWeather.collect {
                when (it) {
                    is Resource.Error -> {
                        Toast.makeText(
                            applicationContext,
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    is Resource.Loading -> {


                    }

                    is Resource.Success -> {
                        adapterForeCastAdapter.differ.submitList(it.data)
                        rvForeCast.adapter = adapterForeCastAdapter
                    }

                    else -> Unit
                }

            }
        }

    }


}

