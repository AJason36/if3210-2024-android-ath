package com.ath.bondoman

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.ath.bondoman.databinding.ActivityAddTransactionBinding
import com.ath.bondoman.model.TransactionCategory
import com.ath.bondoman.viewmodel.LocationViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.view.View
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.ath.bondoman.model.dto.TransactionDTO
import com.ath.bondoman.viewmodel.TransactionViewModel
import androidx.activity.viewModels
import com.ath.bondoman.model.LocationData
import com.ath.bondoman.util.isLocationPermissionGranted
import com.ath.bondoman.util.showLocationPermissionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTransactionActivity : AppCompatActivity() {
    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private lateinit var binding: ActivityAddTransactionBinding
    private val locationViewModel: LocationViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var currentLocation: LocationData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backBtn = binding.addTransactionBackBtn
        backBtn.setOnClickListener {
            finish()
        }

        // Initialize the Spinner for the Category field
        val categories = TransactionCategory.entries.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.addTransactionCategoryField.adapter = adapter

        // Set the default value for the Spinner
        val defaultCategory = TransactionCategory.Income.name
        val spinnerPosition = adapter.getPosition(defaultCategory)
        binding.addTransactionCategoryField.setSelection(spinnerPosition)

        // Observe the location LiveData
        locationViewModel.location.observe(this, Observer { newLocation ->
            currentLocation = newLocation
            binding.addTransactionLocationField.text = currentLocation?.address
        })

        fetchLocation()

        val locationButton = binding.addTransactionLocationButton
        locationButton.setOnClickListener{
            if (isLocationPermissionGranted(this)) {
                fetchLocation()
            } else {
                showLocationPermissionDialog(this, packageName)
            }
        }

        val saveButton = binding.saveTransactionButton
        saveButton.setOnClickListener {
            saveTransaction()
        }

        transactionViewModel.insertResult.observe(this, Observer { rowId ->
            if (rowId != null) {
                Toast.makeText(this, "Transaction inserted successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to insert transaction. Please try again!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationViewModel.fetchLocation(this, fusedLocationClient)
    }

    private fun saveTransaction() {
        val titleField = binding.addTransactionTitleField
        val amountField = binding.addTransactionAmountField
        val categorySpinner = binding.addTransactionCategoryField

        val title = titleField.text.toString()
        val amount = amountField.text.toString()
        val category = TransactionCategory.valueOf(categorySpinner.selectedItem.toString())

        if (title.isEmpty()) {
            titleField.error = "Title cannot be empty"
            return
        }

        if (amount.isEmpty()) {
            amountField.error = "Amount cannot be empty"
            return
        }

        // Create a TransactionDTO with the entered data
        val transactionDTO = TransactionDTO(title, category, amount.toDouble(), currentLocation)

        // Get a reference to the ViewModel and insert the transaction
        transactionViewModel.insertTransaction(transactionDTO)
    }
}
