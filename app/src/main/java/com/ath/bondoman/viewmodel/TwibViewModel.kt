package com.ath.bondoman.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TwibbonViewModel : ViewModel() {
    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> = _imageBitmap


    private val _text = MutableLiveData<String>().apply {
        value = "This is twibbon Fragment"
    }
    val text: LiveData<String> = _text
    fun updateImageBitmap(bitmap: Bitmap) {
        _imageBitmap.value = bitmap
    }

}