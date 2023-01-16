package com.tgapps.weatherapp.services.networking

class Repository(private val apiService: ApiService) {
    suspend fun getCurrentWeather(city: String) = apiService.getCurrentWeather(city)
}