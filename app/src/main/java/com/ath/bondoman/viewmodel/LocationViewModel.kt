package com.ath.bondoman.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    // LiveData to hold the location
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    fun updateLocation(newLocation: String) {
        _location.value = newLocation
    }
}
