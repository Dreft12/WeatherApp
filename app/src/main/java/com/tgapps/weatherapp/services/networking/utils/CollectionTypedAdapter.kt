package com.tgapps.weatherapp.services.networking.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class CollectionTypedAdapter : JsonSerializer<Collection<*>> {
    override fun serialize(
        src: Collection<*>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) // exclusion is made here
            return null

        val array = JsonArray()

        for (child in src) {
            val element = context!!.serialize(child)
            array.add(element)
        }

        return array
    }
}