package com.ath.bondoman.model

import android.os.Parcelable
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationData(
    val latitude: Double = -6.891130664299551,
    val longitude: Double = 107.6106074222042,
    val address: String = "Jl. Ganesa No.10, Lb. Siliwangi, Kecamatan Coblong, Kota Bandung, Jawa Barat 40132"
) : Parcelable


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