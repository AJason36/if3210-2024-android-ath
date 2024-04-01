package com.ath.bondoman.ui.scan

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ath.bondoman.R
import com.ath.bondoman.api.UploadClient
import com.ath.bondoman.databinding.FragmentScanBinding
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dto.ApiResponse
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.repository.TransactionRepository
import com.ath.bondoman.util.isNetworkAvailable
import com.ath.bondoman.viewmodel.CoroutinesErrorHandler
import com.ath.bondoman.viewmodel.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private val scanViewModel :ScanViewModel by viewModels()

    @Inject lateinit var transactionRepository:TransactionRepository
    private lateinit var transaction:Transaction

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraSelector: CameraSelector


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val imageView: ImageView = binding.capturedImage
//            imageView.setImageURI(uri)
            uploadBill(uriToFile(uri,requireContext()))
            // Update the ViewModel
        }
    }

    private fun uriToFile(selectedImgUri: Uri, context: Context): File {
        val contentResolver = context.contentResolver

        // Create a file to write to
        val tempFile = createImageFile(context)

        try {
            val inputStream = contentResolver.openInputStream(selectedImgUri) ?: return tempFile
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return tempFile
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    @Inject
    lateinit var uploadClient: UploadClient

    @Inject
    lateinit var tokenRepository: TokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentScanBinding.inflate(layoutInflater)
    }

    private fun uploadBill(file: File){
        Log.d("[BACKGROUND SERVICE]", "Starting upload bill")
        val isConnected = isNetworkAvailable(requireContext())
        if (isConnected) {
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "file.jpg", requestFile)
            val token = tokenRepository.getToken()
            Log.d("RequestBody", "File Name: ${file.name}")
            Log.d("RequestBody", "Request File: $requestFile")
            Log.d("RequestBody", "Token: ${token?.token}")
            if(token!=null) {
                scanViewModel.upload(token.token,body, object : CoroutinesErrorHandler {
                    override fun onError(message: String) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            Toast.makeText(requireContext(), "No connection available", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun saveBill(){
        transactionRepository.insert(transaction)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        }

        // output: file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val bitmap = imageProxyToBitmap(image)
                    image.close()
                    val photoFile = saveBitmapToFile(bitmap)
                    uploadBill(photoFile)
                    // Update the ViewModel
                }
            }
        )
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // back camera as a default
            cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext() ))
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("files", ".jpg", storageDir).apply {
            try {
                FileOutputStream(this).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error saving bitmap to file", e)
            }
        }
    }
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    // To open the gallery
    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    // Switch camera
    private fun switchCamera(){
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Re-bind use cases
        startCamera()
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val adapter = ItemListAdapter()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        val imageCaptureButton = binding.imageCaptureButton
        // Set up the listeners for take photo
        imageCaptureButton.setOnClickListener {
            Toast.makeText(requireContext(),"Scanning photo..",Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                takePhoto()
            }
        }

        val galleryButton = binding.galleryButton
        // Set up the listeners for choose photo
        galleryButton.setOnClickListener {
            pickImageFromGallery()
        }

        val switchButton = binding.switchButton
        // Set up the listeners for switch camera
        switchButton.setOnClickListener{
            switchCamera()
        }

        val retakeButton = binding.retakeButton
        retakeButton.setOnClickListener{
            switchView(true)
        }

        val saveButton = binding.saveButton
        saveButton.setOnClickListener{
            lifecycleScope.launch {
                Toast.makeText(requireContext(),"Saving to transactions..",Toast.LENGTH_SHORT).show()
                saveBill()
                Toast.makeText(requireContext(),"Transaction saved!", Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),"Transaction saved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_scanFragment_to_transactionFragment)
                }
            }

        }

        val recyclerView: RecyclerView = binding.itemsList
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter=adapter

        scanViewModel.uploadResponse.observe(viewLifecycleOwner) {response ->
            when (response) {
                is ApiResponse.Failure -> {
                    val errorMessage = when {
                        response.code == 400 || response.code == 401 -> "Invalid username or password"
                        response.message.contains("Unable to resolve host") -> "Device not connected to the internet"
                        else -> response.message
                    }
                    Toast.makeText(requireContext(),"Failed get Data: ${response.code}, ${response.message}",Toast.LENGTH_SHORT).show()
                }

                is ApiResponse.Success -> {
                    val itemsList = response.data.items.items
                    adapter.setItems(itemsList)
                    transaction = scanViewModel.createTransactionFromItems(itemsList)
                    switchView(false)
                    Toast.makeText(requireContext(),"Scan complete",Toast.LENGTH_SHORT).show()
                }

                else -> {
                }
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        val imageView: ImageView= binding.capturedImage
        scanViewModel.imageBitmap.observe(viewLifecycleOwner){
            imageView.setImageBitmap(it)
        }

        return root
    }

    private fun switchView(toCamera:Boolean){
        if(toCamera){
            binding.itemsList.visibility=View.GONE
            binding.actionButtonsLayout.visibility=View.GONE
            binding.heading.visibility=View.GONE

            startCamera()
            binding.viewFinder.visibility=View.VISIBLE
            binding.galleryButton.visibility=View.VISIBLE
            binding.switchButton.visibility=View.VISIBLE
            binding.imageCaptureButton.visibility= View.VISIBLE
        }else{
            binding.itemsList.visibility=View.VISIBLE
            binding.actionButtonsLayout.visibility=View.VISIBLE
            binding.heading.visibility=View.VISIBLE

            binding.viewFinder.visibility=View.GONE
            binding.galleryButton.visibility=View.GONE
            binding.switchButton.visibility=View.GONE
            binding.imageCaptureButton.visibility= View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
            }.toTypedArray()
    }
}