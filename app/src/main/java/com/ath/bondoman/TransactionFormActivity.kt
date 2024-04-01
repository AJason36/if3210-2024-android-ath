package com.ath.bondoman

import android.R
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ath.bondoman.databinding.ActivityTransactionFormBinding
import com.ath.bondoman.model.TransactionCategory
import com.ath.bondoman.viewmodel.LocationViewModel
import com.google.android.gms.location.LocationServices
import android.widget.Toast
import com.ath.bondoman.viewmodel.TransactionViewModel
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.ath.bondoman.model.LocationData
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dto.InsertTransactionDTO
import com.ath.bondoman.model.dto.UpdateTransactionDTO
import com.ath.bondoman.util.isLocationPermissionGranted
import com.ath.bondoman.util.showLocationPermissionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionFormActivity : AppCompatActivity() {
    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val EXTRA_TRANSACTION = "EXTRA_TRANSACTION"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val EXTRA_RANDOM_AMOUNT = "EXTRA_RANDOM_AMOUNT"
        const val MODE_ADD = 0
        const val MODE_EDIT = 1
    }

    private lateinit var binding: ActivityTransactionFormBinding
    private val locationViewModel: LocationViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var currentLocation: LocationData? = null

    private var mode: Int = MODE_ADD
    private var transaction: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getIntExtra(EXTRA_MODE, MODE_ADD)

        if (mode == MODE_EDIT) {
            transaction = intent.getParcelableExtra(EXTRA_TRANSACTION)

            // Set title to Update Transaction
            binding.transactionFormHeaderTitle.text = "Edit Transaction"

            // Date
            binding.transactionFormDateLabel.text = transaction?.date

            // Title
            binding.transactionFormTitleField.setText(transaction?.title)

            // Amount
            binding.transactionFormAmountField.setText(transaction?.amount?.toString())

            // Category, not editable
            val categoryPosition = transaction?.category?.ordinal ?: 0
            binding.transactionFormCategoryField.setSelection(categoryPosition)
            binding.transactionFormCategoryField.isEnabled = false

            // Location
            binding.transactionFormLocationField.text = transaction?.location?.address ?: "No location specified"
            currentLocation = transaction?.location
        } else {
            // Hide date
            binding.transactionFormDateLabel.visibility = View.GONE

            // Hide delete button
            binding.deleteTransactionButton.visibility = View.GONE

            // Check if Activity started by Broadcast Receiver
            if (intent.hasExtra(EXTRA_RANDOM_AMOUNT)) {
                val randomAmount = intent.getLongExtra(EXTRA_RANDOM_AMOUNT, 0)
                binding.transactionFormAmountField.setText(randomAmount.toString())
            }

            fetchLocation()
        }

        val backBtn = binding.transactionFormBackBtn
        backBtn.setOnClickListener {
            onBackPressed()
        }

        // Initialize the Spinner for the Category field
        val categories = TransactionCategory.entries.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.transactionFormCategoryField.adapter = adapter

        // Set the default value for the Spinner
        val defaultCategory = TransactionCategory.Income.name
        val spinnerPosition = adapter.getPosition(defaultCategory)
        binding.transactionFormCategoryField.setSelection(spinnerPosition)

        // Observe the location LiveData
        locationViewModel.location.observe(this, Observer { newLocation ->
            if (newLocation != null && !this.isFinishing) {
                currentLocation = newLocation
                binding.transactionFormLocationField.text = currentLocation?.address
            }
        })

        val locationButton = binding.transactionFormLocationButton
        locationButton.setOnClickListener{
            fetchLocation(true)
        }

        val saveButton = binding.saveTransactionButton
        saveButton.setOnClickListener {
            val inputMethodManager = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            saveTransaction()
        }

        val deleteButton = binding.deleteTransactionButton
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes") { dialog, which ->
                    deleteTransaction()
                }
                .setNegativeButton("No", null)
                .show()
        }

        val openGMapsButton = binding.transactionFormOpenInGmapsButton
        openGMapsButton.setOnClickListener {
            currentLocation?.let {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${it.latitude},${it.longitude}(${Uri.encode(it.address)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_LONG).show()
                }
            }
        }

        transactionViewModel.insertResult.observe(this, Observer { rowId ->
            if (rowId != null && rowId != -1L) {
                Toast.makeText(this, "Transaction inserted successfully!", Toast.LENGTH_SHORT).show()
                goToTransactionList()
            } else {
                Toast.makeText(this, "Failed to insert transaction. Please try again!", Toast.LENGTH_SHORT).show()
            }
        })

        transactionViewModel.updateResult.observe(this, Observer { rowId ->
            if (rowId != null && rowId != 0) {
                Toast.makeText(this, "Transaction updated successfully!", Toast.LENGTH_SHORT).show()
                goToTransactionList()
            } else {
                Toast.makeText(this, "Failed to update transaction. Please try again!", Toast.LENGTH_SHORT).show()
            }
        })

        transactionViewModel.deleteResult.observe(this, Observer { rowId ->
            if (rowId != null && rowId != 0) {
                Toast.makeText(this, "Transaction deleted successfully!", Toast.LENGTH_SHORT).show()
                goToTransactionList()
            } else {
                Toast.makeText(this, "Failed to delete transaction. Please try again!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            fetchLocation()
        }
    }

    private fun fetchLocation(askPermission: Boolean = false) {
        if (isLocationPermissionGranted(this)) {
            binding.transactionFormLocationField.text = "Retrieving Location…"

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationViewModel.fetchLocation(this, fusedLocationClient)
        } else {
            binding.transactionFormLocationField.text = "Location is disabled"

            if (askPermission) {
                showLocationPermissionDialog(this, packageName)
            }
        }
    }

    private fun saveTransaction() {
        val titleField = binding.transactionFormTitleField
        val amountField = binding.transactionFormAmountField
        val categorySpinner = binding.transactionFormCategoryField

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

        if (mode == MODE_ADD) {
            val transactionDTO = InsertTransactionDTO(
                title = title,
                category = category,
                amount = amount.toDouble(),
                location = currentLocation
            )
            transactionViewModel.insertTransaction(transactionDTO)
        } else if (mode == MODE_EDIT && transaction != null) {
            val transactionDTO = UpdateTransactionDTO(
                id = transaction!!.id,
                title = title,
                category = category,
                amount = amount.toDouble(),
                location = currentLocation,
                date = transaction!!.date
            )
            transactionViewModel.updateTransaction(transactionDTO)
        }
    }

    private fun deleteTransaction() {
        val localTransaction = transaction
        if (localTransaction != null) {
            transactionViewModel.deleteTransaction(localTransaction)
        }
    }

    private fun goToTransactionList() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
