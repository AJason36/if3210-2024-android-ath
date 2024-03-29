package com.ath.bondoman.util

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

fun showLocationPermissionDialog(context: Context, packageName: String) {
    AlertDialog.Builder(context)
        .setTitle("Location Permission Needed")
        .setMessage("This app needs the Location permission to get your current location. Please grant the permission.")
        .setPositiveButton("Open Settings") { _, _ ->
            // Intent to open the app's system settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}