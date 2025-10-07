package com.payment.payrowapp.contactpayrow

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.ItemsCountRequest
import com.payment.payrowapp.newpayment.StoreItemsRepository
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.otp.EnterOTPActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.sunmipay.LoadingDialog
import com.payment.payrowapp.utils.BaseActivity
import org.json.JSONObject

var ring: MediaPlayer? = null
private var toggleSelection = ""
private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
private var vatStatus = false
private var serviceCatalogStatus = false
private var scanBarCode = false
private var payRowDigitalFeeStatus = false
private var loadDialog: LoadingDialog? = null
private lateinit var ivPayRowDigFeeON: ImageView
private lateinit var ivScanBarCodeON: ImageView
private lateinit var ivMultiON: ImageView
private lateinit var ivCatalogON: ImageView
private lateinit var btnPayRowDigFee: TextView
private lateinit var btnScanBarCode: TextView

class ServiceActivationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_activation)

       // setSupportActionBar(myToolbar)
        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = getString(R.string.service_activiation)
        ring = MediaPlayer.create(this, R.raw.sound_button_click)
        sharedPreferenceUtil = SharedPreferenceUtil(applicationContext)

        ivPayRowDigFeeON = findViewById<ImageView>(R.id.ivPayRowDigFeeON)
        ivScanBarCodeON = findViewById<ImageView>(R.id.ivScanBarCodeON)
        ivMultiON = findViewById<ImageView>(R.id.ivMultiON)
        ivCatalogON = findViewById<ImageView>(R.id.ivCatalogON)
        btnPayRowDigFee = findViewById<TextView>(R.id.btnPayRowDigFee)
        btnScanBarCode = findViewById<TextView>(R.id.btnScanBarCode)
        // payrow digital fee
        if (sharedPreferenceUtil.getPayRowDigital()) {
            payRowDigitalFeeStatus = false
            ivPayRowDigFeeON.setImageResource(R.drawable.ic_multi_user_enable)
        } else {
            payRowDigitalFeeStatus = true
            ivPayRowDigFeeON.setImageResource(R.drawable.ic_multi_enable_off)
        }

        btnPayRowDigFee.setOnClickListener {
            ring?.start()
            toggleSelection = "PayRow digital fee"
            val intent = Intent(this, EnterOTPActivity::class.java)
            intent.putExtra("channel", "toggle")
            intent.putExtra("service", "digitalFee")
            intent.putExtra("status", payRowDigitalFeeStatus)
            resultLauncher.launch(intent)
        }

        // scanbar code
        if (sharedPreferenceUtil.getScanBarCode()) {
            scanBarCode = false
            ivScanBarCodeON.setImageResource(R.drawable.ic_multi_user_enable)
        } else {
            scanBarCode = true
            ivScanBarCodeON.setImageResource(R.drawable.ic_multi_enable_off)
        }

        btnScanBarCode.setOnClickListener {
            ring?.start()
            toggleSelection = "scan barcode"
            if (sharedPreferenceUtil.getCataLogAmount()&& !sharedPreferenceUtil.getScanBarCode()) {
                showToast(getString(R.string.kindly_disable_service_catalog_to_scanbarcode))
            } else {
                val intent = Intent(this, EnterOTPActivity::class.java)
                intent.putExtra("channel", "toggle")
                intent.putExtra("service", "barCode")
                intent.putExtra("status", scanBarCode)
                resultLauncher.launch(intent)
            }
        }

        // set vat calculator
        if (sharedPreferenceUtil.getVATCalculator()) {
            vatStatus = false
            ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
        } else {
            vatStatus = true
            ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
        }

        ivMultiON.setOnClickListener {
            ring?.start()
            toggleSelection = "vat calculator"
            val intent = Intent(this, EnterOTPActivity::class.java)
            intent.putExtra("channel", "toggle")
            intent.putExtra("service", "vat")
            intent.putExtra("status", vatStatus)
            resultLauncher.launch(intent)
            /*if (sharedPreferenceUtil.getVATCalculator()) {
                sharedPreferenceUtil.setVATCalculator(false)
                ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
            } else {
                sharedPreferenceUtil.setVATCalculator(true)
                ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
            }*/
        }

        // set amount from service catalog
        if (sharedPreferenceUtil.getCataLogAmount()) {
            serviceCatalogStatus = false
            ivCatalogON.setImageResource(R.drawable.ic_multi_user_enable)
        } else {
            serviceCatalogStatus = true
            ivCatalogON.setImageResource(R.drawable.ic_multi_enable_off)
        }

        ivCatalogON.setOnClickListener {
            ring?.start()
            toggleSelection = "service catalog"
            if (sharedPreferenceUtil.getScanBarCode()&&!sharedPreferenceUtil.getCataLogAmount()) {
                showToast(getString(R.string.kindly_disable_scanbarcode_to_activate_servicecatalogue))
            } else {
                if (sharedPreferenceUtil.getCataLogAmount()) {
                    val intent = Intent(this, EnterOTPActivity::class.java)
                    intent.putExtra("channel", "toggle")
                    intent.putExtra("service", "serviceCatalogue")
                    intent.putExtra("status", serviceCatalogStatus)
                    resultLauncher.launch(intent)
                } else {
                    showLoadingDialog("Please wait...")
                    val itemsCountRequest = ItemsCountRequest(
                        sharedPreferenceUtil.getGatewayMerchantID(),
                        sharedPreferenceUtil.getMerchantID()
                    )
                    StoreItemsRepository.getItemCountMutableLiveData(
                        this,
                        itemsCountRequest,
                        sharedPreferenceUtil
                    ) { result ->
                        dismissLoadingDialog()
                        showToast(getString(R.string.something_went_wrong_try))
                    }
                    StoreItemsRepository.getItemCountLiveData()
                        .observeOnce(this@ServiceActivationActivity) {
                            dismissLoadingDialog()
                            if (it?.data != null) {
                                val dataObject = JSONObject(it.data)
                                val gateWayItemCount =
                                    if (dataObject.has("gatewayServCount") && !dataObject.isNull("gatewayServCount")) {
                                        dataObject.optInt("gatewayServCount", 0)
                                    } else {
                                        0
                                    }
                                val posItemCount =
                                    if (dataObject.has("posServCount") && !dataObject.isNull("posServCount")) {
                                        dataObject.optInt("posServCount", 0)
                                    } else {
                                        0
                                    }
                                if (gateWayItemCount > 0 || posItemCount > 0) {
                                    val intent = Intent(this, EnterOTPActivity::class.java)
                                    intent.putExtra("channel", "toggle")
                                    intent.putExtra("service", "serviceCatalogue")
                                    intent.putExtra("status", serviceCatalogStatus)
                                    resultLauncher.launch(intent)
                                } else {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.you_are_unable_to_enable_this_triger),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                showToast(getString(R.string.you_are_unable_to_enable_this_triger))
                            }
                        }
                }
            }
        }

    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (toggleSelection == "service catalog") {
                if (sharedPreferenceUtil.getCataLogAmount()) {
                    sharedPreferenceUtil.setCataLogAmount(false)
                    ivCatalogON.setImageResource(R.drawable.ic_multi_enable_off)
                } else {
                    sharedPreferenceUtil.setCataLogAmount(true)
                    ivCatalogON.setImageResource(R.drawable.ic_multi_user_enable)
                }
            } else if (toggleSelection == "vat calculator") {
                if (sharedPreferenceUtil.getVATCalculator()) {
                    sharedPreferenceUtil.setVATCalculator(false)
                    ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
                } else {
                    sharedPreferenceUtil.setVATCalculator(true)
                    ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
                }
            } else if (toggleSelection == "scan barcode") {
                if (sharedPreferenceUtil.getScanBarCode()) {
                    sharedPreferenceUtil.setScanBarCode(false)
                    ivScanBarCodeON.setImageResource(R.drawable.ic_multi_enable_off)
                } else {
                    sharedPreferenceUtil.setScanBarCode(true)
                    ivScanBarCodeON.setImageResource(R.drawable.ic_multi_user_enable)
                }
            } else if (toggleSelection == "PayRow digital fee") {
                if (sharedPreferenceUtil.getPayRowDigital()) {
                    sharedPreferenceUtil.setPayRowDigital(false)
                    ivPayRowDigFeeON.setImageResource(R.drawable.ic_multi_enable_off)
                } else {
                    sharedPreferenceUtil.setPayRowDigital(true)
                    ivPayRowDigFeeON.setImageResource(R.drawable.ic_multi_user_enable)
                }
            }
        }
    }

    private fun showLoadingDialog(msg: String) {
        if (loadDialog == null) {
            loadDialog = LoadingDialog(this, msg)
        } else {
            loadDialog?.setMessage(msg)
        }
        if (!loadDialog!!.isShowing) {
            loadDialog!!.show()
        }
    }

    private fun dismissLoadingDialog() {
        if (loadDialog != null && loadDialog!!.isShowing) {
            loadDialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoadingDialog()
    }
}