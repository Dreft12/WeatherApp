package com.tgapps.weatherapp.services.networking.utils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class DoubleTypedAdapter: JsonSerializer<Double> {
    override fun serialize(
        src: Double?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return if (src == null || src == -1.0) null else JsonPrimitive(src)
    }
}
