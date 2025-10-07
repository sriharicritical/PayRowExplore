package com.payment.payrowapp.qrcodescan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityOrderQrscanBinding
import com.payment.payrowapp.databinding.ActivityQrCodeScanBinding
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.utils.BaseActivity
import java.io.Serializable


class QRCodeScanActivity : BaseActivity() {
    var qrCodeScanActivityViewModel: QRCodeScanActivityViewModel? = null
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var activityQrCodescanBinding:ActivityQrCodeScanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_qr_code_scan)

        activityQrCodescanBinding = ActivityQrCodeScanBinding.inflate(layoutInflater)
        setContentView(activityQrCodescanBinding.root)

        val myToolbar = activityQrCodescanBinding.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Scan Barcode"

        qrCodeScanActivityViewModel = ViewModelProvider(
            this,
            QRCodeScanActivityViewModelFactory(this)
        ).get(QRCodeScanActivityViewModel::class.java)

        val scanIntegrator = IntentIntegrator(this)
        scanIntegrator.setPrompt("Scan")
        scanIntegrator.setBeepEnabled(true)
        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        scanIntegrator.captureActivity = CaptureActivity::class.java
        scanIntegrator.setOrientationLocked(true)
        scanIntegrator.setBarcodeImageEnabled(true)
        scanIntegrator.initiateScan()

    }


    override fun onDestroy() {
        super.onDestroy()
        //  cameraSource.stop()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        Log.i("Scan", "resultCode->$resultCode")
        if (scanningResult != null) {
            Log.i("Scan", "Amount" + scanningResult.contents)
            if (scanningResult.contents != null) {
                if (scanningResult.contents.contains("|")) {
                    val intent = Intent()
                    intent.putExtra("orderAmount", scanningResult.contents.toString())
                    setResult(3, intent)
                    finish()
                } else {
                    // val parts: List<String> = scanningResult.contents.toString().split("/")
                    // val id = parts[parts.size - 1]
                    val parts = scanningResult.contents.toString().split("=")
                    val id = parts.last()
                    qrCodeScanActivityViewModel?.getQrCodeInvoice(id)
                    qrCodeScanActivityViewModel?.getQrCodeInvoiceLiveData()
                        ?.observeOnce(this@QRCodeScanActivity) {
                            if (it?.data != null && it.data[0].checkoutStatus.isNullOrEmpty() && (it.data.get(
                                    0
                                ).checkoutStatus == "CAPTURED"
                                        || it.data.get(0).checkoutStatus == "PARTIAL APPROVED")
                            ) {
                                showToast("Payment already done.")
                                finish()
                            } else {
                                val intent = Intent()
                                if (it?.data != null && it.data.get(0).purchaseBreakdown.service.size > 0) {
                                    Log.i("QRCODE", "data->" + Gson().toJson(it.data).toString())
                                    intent.putExtra(
                                        "itemLength",
                                        it.data.get(0).purchaseBreakdown.service.size
                                    )
                                    intent.putExtra(
                                        "totalAmount",
                                        it.data.get(0).amount
                                    )
                                    intent.putExtra(
                                        "purchaseBreakDown",
                                        it.data.get(0).purchaseBreakdown as Serializable
                                    )
                                    intent.putExtra(
                                        "OrderNumber",
                                        it.data.get(0).orderNumber
                                    )
                                    intent.putExtra(
                                        "MerchantBankURL",
                                        it.data.get(0).merchantBankTransferReturnUrl
                                    )
                                    intent.putExtra(
                                        "mainMerchantId",
                                        it.data.get(0).mainMerchantId
                                    )
                                    intent.putExtra(
                                        "customerEmail",
                                        it.data.get(0).customerEmail
                                    )
                                    intent.putExtra(
                                        "customerPhone",
                                        it.data.get(0).customerPhone
                                    )

                                    intent.putExtra(
                                        "posId",
                                        it.data.get(0).posId
                                    )

                                    intent.putExtra(
                                        "posType",
                                        it.data.get(0).posType
                                    )
                                    intent.putExtra(
                                        "payrowInvoiceNo",
                                        it.data.get(0).payrowInvoiceNo
                                    )
                                    intent.putExtra(
                                        "trnNo",
                                        it.data.get(0).trnNo
                                    )
                                    intent.putExtra(
                                        "receiptNo",
                                        it.data.get(0).receiptNo
                                    )

                                    intent.putExtra(
                                        "merchantEmail",
                                        it.data.get(0).merchantEmail
                                    )

                                    intent.putExtra(
                                        "userId",
                                        it.data.get(0).userId
                                    )
                                    intent.putExtra(
                                        "distributorId",
                                        it.data.get(0).distributorId
                                    )

                                    intent.putExtra(
                                        "storeId",
                                        it.data.get(0).storeId
                                    )

                                    intent.putExtra(
                                        "customerName",
                                        it.data.get(0).customerName
                                    )

                                    intent.putExtra(
                                        "customerBillingCountry",
                                        it.data.get(0).customerBillingCountry
                                    )

                                    intent.putExtra(
                                        "customerBillingCity",
                                        it.data.get(0).customerBillingCity
                                    )
                                    intent.putExtra(
                                        "customerBillingState",
                                        it.data.get(0).customerBillingState
                                    )

                                    intent.putExtra(
                                        "customerBillingPostalCode",
                                        it.data.get(0).customerBillingPostalCode
                                    )

                                    intent.putExtra(
                                        "urn",
                                        it.data.get(0).urn
                                    )

                                    intent.putExtra(
                                        "merchantSiteUrl",
                                        it.data.get(0).merchantSiteUrl
                                    )

                                    intent.putExtra("checkoutId", it.data.get(0).checkoutId)

                                    setResult(3, intent)
                                    showToast("Items fetched successfully")
                                } else {
                                    showToast("Couldn't fetch the items")
                                    intent.putExtra(
                                        "itemLength",
                                        0
                                    )
                                    setResult(3, intent)
                                }
                                finish()
                            }
                        }
                }
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