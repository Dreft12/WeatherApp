package com.tgapps.weatherapp.models

import com.google.gson.annotations.Expose

data class City(
    val condition: Condition,
    val location: Location,
    val currentWeather: CurrentWeather
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tzId: String,
    val localTimeEpoch: Long,
    val localTime: String
)

data class CurrentWeather(
    @Expose
    val lastUpdatedEpoch: Long,
    @Expose
    val lastUpdate: String,
    @Expose
    val tempC: Double,
    @Expose
    val tempF: Double,
    @Expose
    val isDay: Int,
    @Expose
    val condition: Condition,
    @Expose
    val windMph: Double,
    @Expose
    val windKph: Double,
    @Expose
    val windDegree: Double,
    @Expose
    val windDir: String,
    @Expose
    val pressureMb: Double,
    @Expose
    val pressureIn: Double,
    @Expose
    val precipMm: Double,
    @Expose
    val precipIn: Double,
    @Expose
    val humidity: Int,
    @Expose
    val cloud: Int,
    @Expose
    val feelsLikeC: Double,
    @Expose
    val feelsLikeF: Double,
    @Expose
    val visKm: Double,
    @Expose
    val visMiles: Double,
    @Expose
    val uv: Double,
    @Expose
    val gustMph: Double,
    @Expose
    val gustKph: Double
)