package com.ath.bondoman.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ath.bondoman.VerifyJwtService
import com.ath.bondoman.databinding.FragmentSettingsBinding
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.receiver.TransactionFormBroadcastReceiver
import com.ath.bondoman.repository.TransactionRepository
import com.ath.bondoman.viewmodel.TokenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    @Inject lateinit var transactionRepository: TransactionRepository

    private var _binding: FragmentSettingsBinding? = null
    private val tokenViewModel: TokenViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton = binding.logoutButton
        logoutButton.setOnClickListener {
            tokenViewModel.removeToken()
            val serviceIntent = Intent(requireActivity(), VerifyJwtService::class.java)
            requireActivity().stopService(serviceIntent)
        }

        tokenViewModel.token.observe(viewLifecycleOwner) { token ->
            token?.let {
                binding.emailText.text = token.email
            }
        }


        val sendMailButton = binding.sendEmailButton
        sendMailButton.setOnClickListener{
            lifecycleScope.launch {
                transactionRepository.getAll().collect{
                    transactions -> sendEmail(transactions)
                }
            }
        }

        val randomizeTransactionButton = binding.randomizeTransactionButton
        randomizeTransactionButton.setOnClickListener {
            randomizeTransaction()
        }

        val xlsExportButton = binding.xlsExportButton
        val xlsxExportButton = binding.xlsxExportButton
        var filePath:String
        xlsExportButton.setOnClickListener{
            if (allPermissionsGranted()) {
                Toast.makeText(requireContext(), "Exporting transactions...", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    transactionRepository.getAll().collect{
                        transactions -> exportTransactionsToExcel(transactions,"xls",requireContext())
                    }
                }
            }else{
                requestPermissions()
            }
        }

        xlsxExportButton.setOnClickListener{
            if(allPermissionsGranted()) {
                Toast.makeText(requireContext(), "Exporting transactions...", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    transactionRepository.getAll().collect { transactions ->
                        filePath=exportTransactionsToExcel(transactions, "xlsx", requireContext())
                        Log.d("File Export", filePath.toString())
                    }
                }
            }else{
                requestPermissions()
            }
        }

        return root
    }

    private fun sendEmail(transactions: List<Transaction>) {
        val filePath = exportTransactionsToExcel(transactions,"xlsx",requireContext())
        val file: File = File(filePath)
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().applicationContext.packageName}.provider",
            file
        )
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("application/vnd.ms-excel")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(tokenViewModel.token.value?.email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Transaction List")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Transaction list from BondoMan is attached to this email")
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    private fun randomizeTransaction() {
        val MIN_RANDOM_VALUE = 100_000L
        val MAX_RANDOM_VALUE = 10_000_000L
        val randomAmount = Random.nextLong(MIN_RANDOM_VALUE, MAX_RANDOM_VALUE)

        val intent = Intent(context, TransactionFormBroadcastReceiver::class.java).apply {
            action = TransactionFormBroadcastReceiver.TRANSACTION_FORM_BROADCAST_RECEIVER
            putExtra(TransactionFormBroadcastReceiver.RECEIVE_RANDOM_AMOUNT, randomAmount)
        }

        context?.sendBroadcast(intent)
    }
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in SettingsFragment.REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            }
        }
    private fun requestPermissions() {
        activityResultLauncher.launch(SettingsFragment.REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = SettingsFragment.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }
    private fun exportTransactionsToExcel(transactions: List<Transaction>, fileType: String, context: Context): String {

        val strDate: String =
            SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.getDefault()).format(Date())
        val root = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), ""
        )
        try {
            if (!root.exists()) root.mkdirs()

            val workbook: Workbook = if (fileType == "xlsx") {
                XSSFWorkbook() // For .xlsx files
            } else {
                HSSFWorkbook() // For .xls files
            }
            val path:File = if(fileType=="xlsx"){
                File(root, "/Transaction-$strDate.xlsx")
            }else{
                File(root, "/Transaction-$strDate.xls")
            }
            val outputStream = FileOutputStream(path)

            // Sheet
            val sheet = workbook.createSheet("Transactions")
            val headerRow = sheet.createRow(0)

            // Creating header
            for (i in 0..5) {
                val cell = headerRow.createCell(i)
                cell.setCellValue(when (i) {
                    0 -> "ID"
                    1 -> "Title"
                    2 -> "Category"
                    3 -> "Amount"
                    4 -> "Location"
                    5 -> "Date"
                    else -> ""
                })
            }

            if(fileType=="xlsx") {
                // Header
                val headerStyle = workbook.createCellStyle()
                headerStyle.alignment = HorizontalAlignment.CENTER;
                headerStyle.fillForegroundColor = IndexedColors.BLUE_GREY.getIndex();
                headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND;
                headerStyle.borderTop = BorderStyle.MEDIUM;
                headerStyle.borderBottom = BorderStyle.MEDIUM;
                headerStyle.borderRight = BorderStyle.MEDIUM;
                headerStyle.borderLeft = BorderStyle.MEDIUM;

                // Font
                val font: Font = workbook.createFont()
                font.fontHeightInPoints = 12.toShort()
                font.color = IndexedColors.WHITE.getIndex()
                font.bold = true
                headerStyle.setFont(font)

                for (i in 0..5) {
                    val cell = headerRow.getCell(i)
                    cell.cellStyle = headerStyle
                }
            }

            // Populating the rows with data
            transactions.forEachIndexed { index, transaction ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(transaction.id.toDouble())
                row.createCell(1).setCellValue(transaction.title)
                row.createCell(2).setCellValue(transaction.category.name)
                row.createCell(3).setCellValue(transaction.amount)
                row.createCell(4).setCellValue(transaction.location?.address.toString() ?: "N/A") // Customize based on your LocationData toString() method
                row.createCell(5).setCellValue(transaction.date)

                // Set column width for each column individually
                for (i in 0 until 6) {
                    val cellWidth = when (i) {
                        0 -> transaction.id.toString().length + 5
                        1 -> transaction.title.length + 5
                        2 -> transaction.category.name.length + 10
                        3 -> transaction.amount.toString().length + 10
                        4 -> (transaction.location?.toString()?.length ?: 3) / 2 // Adjust for "N/A" length
                        5 -> transaction.date.length + 10
                        else -> 0
                    }
                    sheet.setColumnWidth(i, cellWidth * 256)
                }
            }

            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            Toast.makeText(requireContext(), "Data successfully saved!", Toast.LENGTH_SHORT).show();

//            return FileProvider.getUriForFile(
//                    context,
//                "${requireContext().applicationContext.packageName}.provider",
//                path
            return path.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                if(Build.VERSION.SDK_INT >32) {
                    Manifest.permission.READ_MEDIA_IMAGES;
                    Manifest.permission.READ_MEDIA_VIDEO
                }else if(Build.VERSION.SDK_INT>29){
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                }else{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ).apply {
            }.toTypedArray()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}