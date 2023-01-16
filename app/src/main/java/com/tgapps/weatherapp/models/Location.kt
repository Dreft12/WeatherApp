package com.tgapps.weatherapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tzId: String,
    @Expose
    @SerializedName("localtime_epoch")
    val localTimeEpoch: Long,
    @Expose
    @SerializedName("localtime")
    val localTime: String
)