package com.example.weatherapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FourdaylistitemBinding
import com.example.weatherapp.modal.WeatherList
import java.text.SimpleDateFormat
import java.util.Locale

class ForeCastAdapter : RecyclerView.Adapter<ForeCastAdapter.ForeCastHolder>() {


    private class ItemDiffCallback : DiffUtil.ItemCallback<WeatherList>() {
        override fun areItemsTheSame(oldItem: WeatherList, newItem: WeatherList): Boolean {
            return oldItem.main == newItem.main && oldItem.dt == newItem.dt && oldItem.weather == newItem.weather
        }

        override fun areContentsTheSame(oldItem: WeatherList, newItem: WeatherList): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, ItemDiffCallback())


    inner class ForeCastHolder(private val binding: FourdaylistitemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecastObject: WeatherList) {
            for (i in forecastObject.weather) {
                binding.weatherDescr.text = i.description!!
            }

            binding.humidity.text = forecastObject.main!!.humidity.toString()
            binding.windSpeed.text = forecastObject.wind?.speed.toString()

            val temperatureFahrenheit = forecastObject.main?.temp
            val temperatureCelsius = (temperatureFahrenheit?.minus(273.15))
            val temperatureFormatted = String.format("%.2f", temperatureCelsius)

            binding.tempDisplayForeCast.text = "$temperatureFormatted °C"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(forecastObject.dtTxt!!)
            val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
            val dateAndDayName = outputFormat.format(date!!)
            binding.dayDateText.text = dateAndDayName

            for (i in forecastObject.weather) {
                binding.imageGraphic.setImageResource(setWeatherIcon(i.icon!!))
                binding.smallIcon.setImageResource(setWeatherIcon(i.icon!!))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForeCastHolder {

        val binding: FourdaylistitemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.fourdaylistitem,
                parent,
                false
            )
        return ForeCastHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ForeCastHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    fun setWeatherIcon(icon: String): Int {
        return when (icon) {
            "01d" -> R.drawable.oned
            "01n" -> R.drawable.onen
            "02d" -> R.drawable.twod
            "02n" -> R.drawable.twon
            "10d" -> R.drawable.tend
            "10n" -> R.drawable.tenn
            "03d", "03n" -> R.drawable.threedn
            "04d", "04n" -> R.drawable.fourdn
            "09d", "09n" -> R.drawable.ninedn
            "11d", "11n" -> R.drawable.elevend
            "13d", "13n" -> R.drawable.thirteend
            "50d", "50n" -> R.drawable.fiftydn
            else -> R.drawable.threedn
        }
    }

}

