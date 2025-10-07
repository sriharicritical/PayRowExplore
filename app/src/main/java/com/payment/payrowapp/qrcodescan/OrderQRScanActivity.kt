package com.payment.payrowapp.qrcodescan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityBarQrcodeScannerBinding
import com.payment.payrowapp.databinding.ActivityOrderQrscanBinding
import com.payment.payrowapp.utils.BaseActivity

class OrderQRScanActivity : BaseActivity() {
    private lateinit var activityOrderQrscanBinding: ActivityOrderQrscanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_order_qrscan)

        activityOrderQrscanBinding = ActivityOrderQrscanBinding.inflate(layoutInflater)
        setContentView(activityOrderQrscanBinding.root)

        val myToolbar = activityOrderQrscanBinding.root.findViewById<Toolbar>(R.id.myToolbar)

        setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Scan Barcode"


        val scanIntegrator = IntentIntegrator(this)
        scanIntegrator.setPrompt("Scan")
        scanIntegrator.setBeepEnabled(true)
        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        scanIntegrator.captureActivity = CaptureActivity::class.java
        scanIntegrator.setOrientationLocked(true)
        scanIntegrator.setBarcodeImageEnabled(true)
        scanIntegrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        Log.i("Scan", "resultCode->$resultCode")
        if (scanningResult != null) {
            Log.i("Scan", "Amount" + scanningResult.contents)
            if (scanningResult.contents != null) {

                val intent = Intent()
                intent.putExtra("orderNumber", scanningResult.contents)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                showToast(getString(R.string.scanning_cancelled))
                finish()
            }
        } else {
            showToast(getString(R.string.scanning_failed))
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}