package com.example.weatherapp.di

import android.content.Context
import com.example.weatherapp.repository.ForecastRepository
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.repository.repositoryImp.ForecastRepositoryImpl
import com.example.weatherapp.repository.repositoryImp.WeatherRepositoryImpl
import com.example.weatherapp.service.WeatherApiService
import com.example.weatherapp.util.SharedPrefs
import com.example.weatherapp.util.Utils.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPrefs {
        return SharedPrefs(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherApiService: WeatherApiService,
        sharedPrefs: SharedPrefs
    ): WeatherRepository {
        return WeatherRepositoryImpl(weatherApiService, sharedPrefs)
    }

    @Provides
    @Singleton
    fun provideForecastRepository(
        weatherApiService: WeatherApiService,
        sharedPrefs: SharedPrefs
    ): ForecastRepository {
        return ForecastRepositoryImpl(weatherApiService, sharedPrefs)
    }

}