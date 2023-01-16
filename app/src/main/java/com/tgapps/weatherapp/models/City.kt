package com.tgapps.weatherapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class City(
    val location: Location,
    val current: CurrentWeather
)