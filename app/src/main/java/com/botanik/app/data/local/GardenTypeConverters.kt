package com.botanik.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Serializa Map<String, String> y List<String> a JSON para almacenarlos
 * como columnas de texto en Room. Usa Gson (ya presente en el stack vía
 * Firestore), NO Moshi.
 */
class GardenTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromCareMap(map: Map<String, String>): String = gson.toJson(map)

    @TypeConverter
    fun toCareMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromFunFactsList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toFunFactsList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
