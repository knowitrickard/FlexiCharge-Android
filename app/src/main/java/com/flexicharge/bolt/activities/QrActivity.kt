package com.flexicharge.bolt.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.flexicharge.bolt.R
import okio.utf8Size
import java.io.Serializable
import java.lang.Exception


class QrActivity() : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner

    private val NOT_VALID_QR_STRING = "NOT_VALID_QR_STRING"
    private val VALID_QR_STRING_LENGTH = "['1', '2', '3', '4', '5', '6']".length

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            scanQR()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 123) {
            if (!mutableListOf(grantResults).contains<Serializable>(PackageManager.PERMISSION_DENIED)) {
                scanQR()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun validateChargerId(chargerId: String): String {

        if (chargerId.length != VALID_QR_STRING_LENGTH) {
            return NOT_VALID_QR_STRING
        }
        val formattedString = chargerId.filter {
            it.isDigit()
        }
        if(formattedString.length != 6) {
            return NOT_VALID_QR_STRING
        }
        return formattedString
    }


    private fun scanQR() {

        val scannerView: CodeScannerView = findViewById(R.id.qrActivity_codeScannerView)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {

                try {
                    val validatedQRString = validateChargerId(it.text)
                    if (validatedQRString != NOT_VALID_QR_STRING) {
                        setResult(Activity.RESULT_OK, Intent().putExtra("QR_SCAN_RESULT", validatedQRString))
                        finish()
                    } else {
                        Toast.makeText(this, "QR INVALID", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {

                }
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Camera initialization error : ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(::codeScanner.isInitialized){
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        if(::codeScanner.isInitialized){
            codeScanner.releaseResources()
        }
    }

}