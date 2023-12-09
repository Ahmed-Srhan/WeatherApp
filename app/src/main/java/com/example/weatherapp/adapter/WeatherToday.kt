package com.example.weatherapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.TodayforecastlistBinding
import com.example.weatherapp.modal.WeatherList
import java.text.SimpleDateFormat
import java.util.Calendar

class WeatherToday : RecyclerView.Adapter<WeatherToday.TodayHolder>() {

    private class ItemDiffCallback : DiffUtil.ItemCallback<WeatherList>() {
        override fun areItemsTheSame(oldItem: WeatherList, newItem: WeatherList): Boolean {
            return oldItem.main == newItem.main && oldItem.dt == newItem.dt && oldItem.weather == newItem.weather
        }

        override fun areContentsTheSame(oldItem: WeatherList, newItem: WeatherList): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, ItemDiffCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayHolder {

        val binding: TodayforecastlistBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.todayforecastlist,
                parent,
                false
            )
        return TodayHolder(binding)


    }


    inner class TodayHolder(private val binding: TodayforecastlistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(todayForeCast: WeatherList) {

            binding.timeDisplay.text = todayForeCast.dtTxt!!.substring(11, 16).toString()


            val temperatureFahrenheit = todayForeCast.main?.temp
            val temperatureCelsius = (temperatureFahrenheit?.minus(273.15))
            val temperatureFormatted = String.format("%.2f", temperatureCelsius)
            binding.tempDisplay.text = "$temperatureFormatted °C"


            val calendar = Calendar.getInstance()

            // Define the desired format
            val dateFormat = SimpleDateFormat("HH::mm")
            val formattedTime = dateFormat.format(calendar.time)
            val timeOfApi = todayForeCast.dtTxt!!.split(" ")
            val partAfterSpace = timeOfApi[1]

            Log.e("time", " formatted time:${formattedTime}, timeofapi: ${partAfterSpace}")


            for (i in todayForeCast.weather) {
                binding.imageDisplay.setImageResource(getWeatherIconResource(i.icon!!))
            }

        }


    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    override fun onBindViewHolder(holder: TodayHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    fun getWeatherIconResource(icon: String): Int {
        return when (icon) {
            "01d" -> R.drawable.oned
            "01n" -> R.drawable.onen
            "02d" -> R.drawable.twod
            "02n" -> R.drawable.twon
            "03d", "03n" -> R.drawable.threedn
            "10d" -> R.drawable.tend
            "10n" -> R.drawable.tenn
            "04d", "04n" -> R.drawable.fourdn
            "09d", "09n" -> R.drawable.ninedn
            "11d", "11n" -> R.drawable.elevend
            "13d", "13n" -> R.drawable.thirteend
            "50d", "50n" -> R.drawable.fiftydn
            else -> R.drawable.threedn
        }
    }


}








