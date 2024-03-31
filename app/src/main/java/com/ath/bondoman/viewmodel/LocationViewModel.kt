package com.ath.bondoman.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ath.bondoman.model.LocationData
import com.ath.bondoman.util.isLocationPermissionGranted
import java.util.Locale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest

class LocationViewModel : ViewModel() {
    private val _location = MutableLiveData<LocationData>()
    val location: LiveData<LocationData> get() = _location

    @SuppressLint("MissingPermission")
    fun fetchLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        if (isLocationPermissionGranted(context)) {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses: MutableList<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address: String = if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) else "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                        _location.value = LocationData(location.latitude, location.longitude, address)
                    }
                }
        }
    }
}
