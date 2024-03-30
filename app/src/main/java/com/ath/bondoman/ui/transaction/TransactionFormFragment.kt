package com.ath.bondoman.ui.transaction

import android.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ath.bondoman.databinding.FragmentTransactionFormBinding
import com.ath.bondoman.model.LocationData
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.TransactionCategory
import com.ath.bondoman.model.dto.InsertTransactionDTO
import com.ath.bondoman.model.dto.UpdateTransactionDTO
import com.ath.bondoman.util.isLocationPermissionGranted
import com.ath.bondoman.util.showLocationPermissionDialog
import com.ath.bondoman.viewmodel.LocationViewModel
import com.ath.bondoman.viewmodel.TransactionViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionFormFragment : Fragment() {
    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val EXTRA_TRANSACTION = "EXTRA_TRANSACTION"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val MODE_ADD = 0
        const val MODE_EDIT = 1
    }

    private var _binding: FragmentTransactionFormBinding? = null
    private val binding get() = _binding!!
    private val locationViewModel: LocationViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var currentLocation: LocationData? = null

    private var mode: Int = MODE_ADD
    private var transaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mode = arguments?.getInt(EXTRA_MODE, MODE_ADD) ?: MODE_ADD

        if (mode == MODE_EDIT) {
            transaction = arguments?.getParcelable(EXTRA_TRANSACTION)

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
        } else {
            // Hide date
            binding.transactionFormDateLabel.visibility = View.GONE

            // Hide delete button
            binding.deleteTransactionButton.visibility = View.GONE

            fetchLocation()
        }

        val backBtn = binding.transactionFormBackBtn
        backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Initialize the Spinner for the Category field
        val categories = TransactionCategory.entries.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.transactionFormCategoryField.adapter = adapter

        // Set the default value for the Spinner
        val defaultCategory = TransactionCategory.Income.name
        val spinnerPosition = adapter.getPosition(defaultCategory)
        binding.transactionFormCategoryField.setSelection(spinnerPosition)

        // Observe the location LiveData
        locationViewModel.location.observe(viewLifecycleOwner, Observer { newLocation ->
            if (newLocation != null) {
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
            AlertDialog.Builder(requireContext())
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
            val location = if (mode == MODE_ADD) {
                currentLocation
            } else {
                transaction?.location
            }
            location?.let {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${it.latitude},${it.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(requireContext(), "Google Maps is not installed", Toast.LENGTH_LONG).show()
                }
            }
        }

        transactionViewModel.insertResult.observe(viewLifecycleOwner, Observer { rowId ->
            if (rowId != null && rowId != -1L) {
                Toast.makeText(requireContext(), "Transaction inserted successfully!", Toast.LENGTH_SHORT).show()
                goToTransactionList()
            } else {
                Toast.makeText(requireContext(), "Failed to insert transaction. Please try again!", Toast.LENGTH_SHORT).show()
            }
        })

        transactionViewModel.updateResult.observe(viewLifecycleOwner, Observer { rowId ->
            if (rowId != null && rowId != 0) {
                Toast.makeText(requireContext(), "Transaction updated successfully!", Toast.LENGTH_SHORT).show()
                goToTransactionList()
            } else {
                Toast.makeText(requireContext(), "Failed to update transaction. Please try again!", Toast.LENGTH_SHORT).show()
            }
        })

        transactionViewModel.deleteResult.observe(viewLifecycleOwner, Observer { rowId ->
            if (rowId != null && rowId != 0) {
                Toast.makeText(requireContext(), "Transaction deleted successfully!", Toast.LENGTH_SHORT).show()
                goToTransactionList()
            } else {
                Toast.makeText(requireContext(), "Failed to delete transaction. Please try again!", Toast.LENGTH_SHORT).show()
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
        if (isLocationPermissionGranted(requireActivity())) {
            binding.transactionFormLocationField.text = "Retrieving Location…"

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            locationViewModel.fetchLocation(requireActivity(), fusedLocationClient)
        } else {
            binding.transactionFormLocationField.text = "Location is disabled"

            if (askPermission) {
                showLocationPermissionDialog(requireActivity(), requireActivity().packageName)
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
        findNavController().navigate(com.ath.bondoman.R.id.navigation_transaction)
    }

    override fun onResume() {
        super.onResume()
        val navView: BottomNavigationView? = activity?.findViewById(com.ath.bondoman.R.id.nav_view)
        navView?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        val navView: BottomNavigationView? = activity?.findViewById(com.ath.bondoman.R.id.nav_view)
        navView?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

