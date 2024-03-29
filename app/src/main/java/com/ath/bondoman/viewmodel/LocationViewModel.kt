package com.ath.bondoman.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ath.bondoman.model.LocationData
import com.ath.bondoman.util.isLocationPermissionGranted
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.internal.Contexts.getApplication
import java.util.Locale

class LocationViewModel : ViewModel() {
    private val _location = MutableLiveData<LocationData>()
    val location: LiveData<LocationData> get() = _location

    @SuppressLint("MissingPermission")
    fun fetchLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        if (isLocationPermissionGranted(context)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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
