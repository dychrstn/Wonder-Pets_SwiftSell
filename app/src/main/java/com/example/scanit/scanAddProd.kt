package com.example.scanit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class scanAddProd : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var captureManager: CaptureManager
    private lateinit var uploadButton: ImageButton
    private var cameraId: String? = null
    private var cameraManager: CameraManager? = null

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    val imagePath = getImagePathFromUri(imageUri)
                    // ... continue with image processing logic

                    // Pass the image path to the barcode detection method
                    detectBarcodeFromImage(imagePath)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_add_prod)

        // Initialize database referenc
        barcodeView = findViewById(R.id.barcode_scanner)

        // Initialize CaptureManager
        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)
        captureManager.decode()

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager?.cameraIdList?.get(0)

        // Set the barcode callback
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { itemBarcode ->
                    fetchItemData(itemBarcode)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Handle possible result points
            }
        })

        // Set up the upload button
        uploadButton = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set up the flashlight button
        val button = findViewById<ImageButton>(R.id.flashlightButton)
        button.setOnClickListener {
            toggleFlashlight()
        }

    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun getImagePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun detectBarcodeFromImage(imagePath: String?) {
        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.EAN_13)
            .build()

        if (!barcodeDetector.isOperational) {
            showToast("Could not set up barcode detector")
            return
        }

        val bitmap = BitmapFactory.decodeFile(imagePath)
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val barcodes = barcodeDetector.detect(frame)

        if (barcodes.size() > 0) {
            val barcode = barcodes.valueAt(0)
            val barcodeValue = barcode.rawValue

            val intentAdd = Intent(applicationContext, AddProductActivity::class.java)
            if(intent.getStringExtra("suggestCat") != null){
                val sugCatText = intent.getStringExtra("suggestCat")
                intentAdd.putExtra("suggestCat", sugCatText)
            }
            if(intent.getStringExtra("selectCat") != null){
                val catSpinSel = intent.getStringExtra("selectCat")
                intentAdd.putExtra("selectCat", catSpinSel)
            }
            if(intent.getStringExtra("dateText") != null){
                val dateText = intent.getStringExtra("dateText")
                intentAdd.putExtra("dateText", dateText)
            }
            if(intent.getStringExtra("prodName") != null ){
                val itemName = intent.getStringExtra("prodName")
                intentAdd.putExtra("prodName", itemName)
            }
            if(intent.getStringExtra("prodPrice") != null){
                val itemPrice = intent.getStringExtra("prodPrice")
                intentAdd.putExtra("prodPrice", itemPrice)
            }
            if(intent.getStringExtra("prodCost") != null){
                val itemCost = intent.getStringExtra("prodCost")
                intentAdd.putExtra("prodCost", itemCost)
            }
            if(intent.getStringExtra("prodQuant") != null){
                val itemQty = intent.getStringExtra("prodQuant")
                intentAdd.putExtra("prodQuant", itemQty)
            }


            intentAdd.putExtra("itemBarcode", barcodeValue)

            startActivity(intentAdd)
        } else {
            showToast("No barcode detected in the image")
        }
    }

    private fun fetchItemData(itemBarcode: String) {

        val intentAdd = Intent(applicationContext, AddProductActivity::class.java)
        if(intent.getStringExtra("suggestCat") != null){
            val sugCatText = intent.getStringExtra("suggestCat")
            intentAdd.putExtra("suggestCat", sugCatText)
        }
        if(intent.getStringExtra("selectCat") != null){
            val catSpinSel = intent.getStringExtra("selectCat")
            intentAdd.putExtra("selectCat", catSpinSel)
        }
        if(intent.getStringExtra("dateText") != null){
            val dateText = intent.getStringExtra("dateText")
            intentAdd.putExtra("dateText", dateText)
        }
        if(intent.getStringExtra("prodName") != null ){
            val itemName = intent.getStringExtra("prodName")
            intentAdd.putExtra("prodName", itemName)
        }
        if(intent.getStringExtra("prodPrice") != null){
            val itemPrice = intent.getStringExtra("prodPrice")
            intentAdd.putExtra("prodPrice", itemPrice)
        }
        if(intent.getStringExtra("prodCost") != null){
            val itemCost = intent.getStringExtra("prodCost")
            intentAdd.putExtra("prodCost", itemCost)
        }
        if(intent.getStringExtra("prodQuant") != null){
            val itemQty = intent.getStringExtra("prodQuant")
            intentAdd.putExtra("prodQuant", itemQty)
        }


        intentAdd.putExtra("itemBarcode", itemBarcode)

        startActivity(intentAdd)

    }

    private fun toggleFlashlight() {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), 1)
        } else {
            try {
                val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                if (cameraId != null) {
                    cameraManager?.setTorchMode(cameraId, true)
                    showToast("Flashlight turned on")
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                showToast("Failed to turn on flashlight")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Check and request camera permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            captureManager.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureManager.onResume()
            } else {
                // Handle camera permission denial
                // ...
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}