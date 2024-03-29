package com.ath.bondoman.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

class LocationDataTypeConverter {

    @TypeConverter
    fun fromLocationData(locationData: LocationData?): String? {
        if (locationData == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<LocationData>() {}.type
        return gson.toJson(locationData, type)
    }

    @TypeConverter
    fun toLocationData(locationDataString: String?): LocationData? {
        if (locationDataString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<LocationData>() {}.type
        return gson.fromJson(locationDataString, type)
    }
}