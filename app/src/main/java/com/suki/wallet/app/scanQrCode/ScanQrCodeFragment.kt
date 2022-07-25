package com.suki.wallet.app.scanQrCode

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.innopage.core.base.BaseFragment
import com.innopage.core.webservice.config.Constants
import com.suki.wallet.R
import com.suki.wallet.databinding.FragmentScanQrCodeBinding
import java.io.File
import java.io.IOException

class ScanQrCodeFragment : BaseFragment<ScanQrCodeViewModel>() {
    private var mQrCodeReaderSurfaceView: SurfaceView? = null
    private var mBarcodeDetector: BarcodeDetector? = null
    private var mCameraSource: CameraSource? = null
    private var mIsDecodeEnable: Boolean = false

    override val viewModel: ScanQrCodeViewModel by lazy { setUpViewModel(requireActivity().application) as ScanQrCodeViewModel }
    private lateinit var binding: FragmentScanQrCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scan_qr_code, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.executePendingBindings()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupToolbar(binding.toolbar)
        // Check permission before init Camera
        if (checkCameraPermission()) {
            initCamera()
        } else {
            requestCameraPermission()
        }

        // Set up enable button on click listener
        binding.btnEnable.setOnClickListener {
            requestCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                Constants.ACCESS_CAMERA_REQUEST_CODE -> initCamera()
                Constants.ACCESS_WRITE_EXTERNAL_STORAGE -> showImagePicker()
            }
        } else {
            binding.layoutDisable.visibility = View.VISIBLE
            binding.layoutCamera.visibility = View.INVISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_IMAGE_PICKER ) {
            val imageUri = data?.data
            if (imageUri != null) {
                val file = getFileFromUri(
                    requireContext().contentResolver,
                    imageUri,
                    requireContext().cacheDir
                )
                val absolutePath = file.absolutePath
                scanQrcodeFromImage(absolutePath)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraSource?.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_photo_pick, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.action_album -> {
                if(checkReadExternalStoragePermission()){
                    showImagePicker()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initCamera() {
        // Init view
        mQrCodeReaderSurfaceView = SurfaceView(context)
        binding.layoutQrcodeReader.removeAllViews()
        binding.layoutQrcodeReader.addView(mQrCodeReaderSurfaceView)

        // Init data
        mBarcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        // Set touch focus
        mQrCodeReaderSurfaceView?.setOnClickListener {
            mCameraSource?.apply {
                cameraFocus(this, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            }
        }

        // Add callback for surface view
        mQrCodeReaderSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                try {
                    if (checkCameraPermission()) {
                        mCameraSource = CameraSource.Builder(context, mBarcodeDetector)
                            .setFacing(CameraSource.CAMERA_FACING_BACK)
                            .setAutoFocusEnabled(true)
                            .build()
                        mIsDecodeEnable = true
                        mCameraSource?.start(surfaceHolder)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
            }

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                mIsDecodeEnable = false
                mCameraSource?.stop()
                mCameraSource?.release()
                mCameraSource = null
            }
        })

        // Detect qrcode
        mBarcodeDetector?.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    barcodes.valueAt(0).displayValue.takeIf { mIsDecodeEnable }?.apply {
                        val intent = Intent()
                        intent.putExtra(Constants.EXTRA_WALLET_ADDRESS, this)
                        activity?.setResult(Activity.RESULT_OK, intent)
                        activity?.finish()
                    }
                }
            }
        })

        // Set up visibility
        binding.layoutDisable.visibility = View.INVISIBLE
        binding.layoutCamera.visibility = View.VISIBLE
    }

    private fun cameraFocus(cameraSource: CameraSource, focusMode: String): Boolean {
        val declaredFields = CameraSource::class.java.declaredFields

        for (field in declaredFields) {
            if (field.type == Camera::class.java) {
                field.isAccessible = true
                try {
                    val camera = field.get(cameraSource) as? Camera
                    if (camera != null) {
                        val params = camera.parameters
                        params.focusMode = focusMode
                        camera.parameters = params
                        return true
                    }

                    return false
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

                break
            }
        }

        return false
    }

    // Request CAMERA permission
    private fun requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                Array(1) { Manifest.permission.CAMERA },
                Constants.ACCESS_CAMERA_REQUEST_CODE
            )
            return
        }
    }

    // Check CAMERA permission
    private fun checkCameraPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun checkReadExternalStoragePermission(): Boolean {
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    Constants.ACCESS_WRITE_EXTERNAL_STORAGE
                )
                return false
            }
        }
        return true
    }

    private fun showImagePicker(){
        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT)

        intent.type = "image/*"
        startActivityForResult(intent, Constants.REQUEST_IMAGE_PICKER)
    }

    private fun scanQrcodeFromImage(path: String) {
        BitmapFactory.decodeFile(path)?.apply {
            val frame = Frame.Builder().setBitmap(this).build()
            val barCodes = mBarcodeDetector?.detect(frame)

            if (barCodes != null && barCodes.size() > 0) {
                barCodes.valueAt(0).rawValue.apply {
                    val intent = Intent()
                    intent.putExtra(Constants.EXTRA_WALLET_ADDRESS, this)
                    activity?.setResult(Activity.RESULT_OK, intent)
                    activity?.finish()
                }
            } else {
                Toast.makeText(requireContext(), "Cannot detect QR code, please try again!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFileFromUri(contentResolver: ContentResolver, uri: Uri, directory: File): File {
        val currentMilliSecond = System.currentTimeMillis().toString()
        val file =
            File.createTempFile(currentMilliSecond, ".jpg", directory)
        file.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it)
        }
        file.deleteOnExit()

        return file
    }

    private fun setupToolbar(toolbar: Toolbar) {
        (activity as AppCompatActivity).run {
            setSupportActionBar(toolbar)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_nav_close)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.scan_qr_code)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }
}