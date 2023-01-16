package com.tgapps.weatherapp.services.networking

import com.tgapps.weatherapp.BuildConfig.WEATHER_API_KEY
import com.tgapps.weatherapp.models.City
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/v1/current.json?key=$WEATHER_API_KEY")
    suspend fun getCurrentWeather(@Query("q") ciudad: String): Response<City>
}