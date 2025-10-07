package com.payment.payrowapp.qrcodescan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityBarQrcodeScannerBinding
import com.payment.payrowapp.databinding.ActivityContactBinding
import com.payment.payrowapp.dataclass.BarCodeItem
import com.payment.payrowapp.dataclass.SharedData
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity

import org.json.JSONObject

class BarQRCodeScannerActivity : BaseActivity() {
    private var qrCodeScanActivityViewModel: QRCodeScanActivityViewModel? = null
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var itemSelection = "PosItems"
    private lateinit var activityBarCodeScanner: ActivityBarQrcodeScannerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_bar_qrcode_scanner)
        activityBarCodeScanner = ActivityBarQrcodeScannerBinding.inflate(layoutInflater)
        setContentView(activityBarCodeScanner.root)

        val myToolbar = activityBarCodeScanner.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Scan Barcode"

        val bundle = intent.extras
        itemSelection = bundle?.getString("itemSelection").toString()

        sharedPreferenceUtil = SharedPreferenceUtil(this)

        qrCodeScanActivityViewModel = ViewModelProvider(
            this,
            QRCodeScanActivityViewModelFactory(this)
        )[QRCodeScanActivityViewModel::class.java]

        startBarcodeScanner()
    }

    private fun startBarcodeScanner() {
        val options = ScanOptions().apply {
            setPrompt("Scan a barcode")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setBarcodeImageEnabled(true)
        }
        barcodeLauncher.launch(options)
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result != null && result.contents != null) {
            if (itemSelection == "GatewayItems") {
                qrCodeScanActivityViewModel?.getGatewayBarCodeItem(result.contents)
                qrCodeScanActivityViewModel?.getGatewayBarCodeItemLiveData()
                    ?.observeOnce(this@BarQRCodeScannerActivity) {
                        if (it?.data != null) {
                            val dataObject = JSONObject(it.data)
                            /* if (dataObject.has("merchantId") && dataObject.getString("merchantId")
                                     .equals(
                                         sharedPreferenceUtil.getMerchantID()
                                     )
                             ) {*/
                            val barCodeItem = BarCodeItem(
                                dataObject.getString("serviceId"),
                                dataObject.getString("shortServiceName"),
                                dataObject.getString("unitPrice"),
                                1,
                                dataObject.getString("unitPrice").toDouble(),
                                dataObject.getString("categoryId")
                            )

                            val existingProduct =
                                SharedData.barCodeItems.find { it1 -> it1.serviceCode == barCodeItem.serviceCode }
                            if (existingProduct != null) {
                                existingProduct.quantity++
                                existingProduct.transactionAmount =
                                    existingProduct.quantity * existingProduct.unitPrice.toDouble()
                            } else {
                                SharedData.barCodeItems.add(barCodeItem)
                            }
                            val resultIntent = Intent()
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                            /* } else {
                                 showToast(getString(R.string.this_item_not_available_this_merchant))
                                 finish()
                             }*/
                        } else {
                            showToast(getString(R.string.data_not_available))
                            finish()
                        }
                    }
            } else if (itemSelection == "PosItems") {
                qrCodeScanActivityViewModel?.getQrCodeItem(result.contents)
                qrCodeScanActivityViewModel?.getBarCodeItemLiveData()
                    ?.observeOnce(this@BarQRCodeScannerActivity) {
                        if (it?.data != null) {
                            val dataObject = JSONObject(it.data)
                            if (dataObject.has("merchantId") && dataObject.getString("merchantId")
                                    .equals(
                                        sharedPreferenceUtil.getMerchantID()
                                    )
                            ) {
                                val barCodeItem = BarCodeItem(
                                    dataObject.getString("serviceId"),
                                    dataObject.getString("shortServiceName"),
                                    dataObject.getString("unitPrice"),
                                    1,
                                    dataObject.getString("unitPrice").toDouble(),
                                    dataObject.getString("categoryId"))

                                val existingProduct =
                                    SharedData.barCodeItems.find { it1 -> it1.serviceCode == barCodeItem.serviceCode }
                                if (existingProduct != null) {
                                    if (dataObject.getString("serviceType") == "F") {
                                        showToast(getString(R.string.your_unable_add_item_multiple_times))
                                        finish()
                                    } else {
                                        existingProduct.quantity++
                                        existingProduct.transactionAmount =
                                            existingProduct.quantity * existingProduct.unitPrice.toDouble()
                                    }
                                } else {
                                    SharedData.barCodeItems.add(barCodeItem)
                                }
                                //  SharedData.barCodeItems.add(barCodeItem)
                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            } else {
                                showToast(getString(R.string.this_item_not_available_this_merchant))
                                finish()
                            }
                        } else {
                            showToast(getString(R.string.data_not_available))
                            finish()
                        }
                    }
            }
            //   println("Scanned Result: ${result.contents}")
        } else {
            showToast(getString(R.string.scanning_failed))
            finish()
        }
    }
}