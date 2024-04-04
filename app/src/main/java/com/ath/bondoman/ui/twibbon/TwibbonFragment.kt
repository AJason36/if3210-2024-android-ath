package com.ath.bondoman.ui.twibbon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.ath.bondoman.R
import com.ath.bondoman.databinding.FragmentTwibbonBinding
import com.ath.bondoman.viewmodel.TwibbonViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TwibbonFragment : Fragment() {

    private var _binding: FragmentTwibbonBinding? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraSelector: CameraSelector
    private val twibViewModel :TwibbonViewModel by viewModels()
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var currentTwibbonResourceId: Int = R.drawable.twibbon1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentTwibbonBinding.inflate(layoutInflater)

    }
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
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

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
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

                    // Update the ViewModel
                    binding.capturedImage.setImageBitmap(bitmap)
                    switchView(false)
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
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // back camera as a default
            cameraSelector =CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext() ))
    }

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

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTwibbonBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        val imageCaptureButton = binding.imageCaptureButton
        // Set up the listeners for take photo
        imageCaptureButton.setOnClickListener { takePhoto() }

        val switchButton = binding.switchButton
        // Set up the listeners for switch camera
        switchButton.setOnClickListener{
            switchCamera()
        }

        val twibbonImageView = binding.twibbonImageView
        val galleryButton = binding.galleryButton
        // Set up the listeners for choose photo
        galleryButton.setOnClickListener {
            currentTwibbonResourceId = if (currentTwibbonResourceId == R.drawable.twibbon1) {
                R.drawable.twibbon2
            } else {
                R.drawable.twibbon1
            }
            twibbonImageView.setImageResource(currentTwibbonResourceId)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        val imageView: ImageView= binding.capturedImage
        twibViewModel.imageBitmap.observe(viewLifecycleOwner){
            imageView.setImageBitmap(it)
        }

        val retakeBtn: Button=binding.retakeButton
        retakeBtn.setOnClickListener{switchView(true)}

        return root
    }

    private fun switchView(toCamera:Boolean){
        if(toCamera){
            binding.retakeButton.visibility=View.GONE
            binding.capturedImage.visibility=View.GONE

            startCamera()
            binding.previewView.visibility=View.VISIBLE
            binding.switchButton.visibility=View.VISIBLE
            binding.galleryButton.visibility=View.VISIBLE
            binding.imageCaptureButton.visibility= View.VISIBLE
        }else{
            binding.retakeButton.visibility=View.VISIBLE
            binding.capturedImage.visibility=View.VISIBLE

            binding.previewView.visibility=View.GONE
            binding.switchButton.visibility=View.GONE
            binding.galleryButton.visibility=View.GONE
            binding.imageCaptureButton.visibility= View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
            }.toTypedArray()
    }
}