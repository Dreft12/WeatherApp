package com.tgapps.weatherapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun convertToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000) // convert to milliseconds
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date) // format the date as you wish
    }
}