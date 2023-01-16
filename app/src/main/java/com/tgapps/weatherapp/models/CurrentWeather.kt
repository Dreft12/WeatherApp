package com.tgapps.weatherapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @Expose
    @SerializedName("last_updated_epoch")
    val lastUpdatedEpoch: Long,
    @Expose
    @SerializedName("last_updated")
    val lastUpdate: String,
    @Expose
    @SerializedName("temp_c")
    val tempC: Double,
    @Expose
    @SerializedName("temp_f")
    val tempF: Double,
    @Expose
    @SerializedName("is_day")
    val isDay: Int,
    @Expose
    val condition: Condition,
    @Expose
    @SerializedName("wind_mph")
    val windMph: Double,
    @Expose
    @SerializedName("wind_kph")
    val windKph: Double,
    @Expose
    @SerializedName("wind_degree")
    val windDegree: Double,
    @Expose
    @SerializedName("wind_dir")
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
    @SerializedName("humidity")
    val humidity: Int,
    @Expose
    val cloud: Int,
    @Expose
    @SerializedName("feelslike_c")
    val feelsLikeC: Double,
    @Expose
    @SerializedName("feelslike_f")
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