package com.tgapps.weatherapp.services.networking.utils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class LongTypedAdapter: JsonSerializer<Long> {
    override fun serialize(
        src: Long?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return if (src == null || src == -1L) null else JsonPrimitive(src)
    }
}