package com.payment.payrowapp.paymenthistory

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivitySummaryDailySelectionBinding
import com.payment.payrowapp.databinding.ActivitySupportReportBinding
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.dataclass.SummaryDates
import com.payment.payrowapp.dataclass.SummaryReportRequest
import com.payment.payrowapp.invoicerecall.QRCodeReceiptActivity
import com.payment.payrowapp.newpayment.CustomerCopySharedActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SummaryReportActivity : BaseActivity() {
    var ring: MediaPlayer? = null
    var from: String? = null
    var to: String? = null
    var channel: String? = null
    var optionSelected = "Email"

    companion object {
        const val url = ApiClient.BASE_URL_SOFT_POS + ApiClient.SUMMARY_INVOICE
    }

    private lateinit var activitySupportReportBinding: ActivitySupportReportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_support_report)
        activitySupportReportBinding = ActivitySupportReportBinding.inflate(layoutInflater)
        setContentView(activitySupportReportBinding.root)

        setupToolbar()
      //  setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        supportActionBar?.title = bundle?.getString("Heading").toString()
        val sharedPreferenceUtil = SharedPreferenceUtil(this@SummaryReportActivity)

        when (bundle?.getString("Heading")) {
            "Tap To Pay" -> channel = "Card"
            "Cash Invoice" -> channel = "Cash"
            "Pay By Link" -> channel = "Paybylink"
            "Pay By QR Code" -> channel = "generateQR"
        }

        if (channel == "Cash") {
            activitySupportReportBinding.tvVoidLabel.visibility = View.GONE
            activitySupportReportBinding.tvVoidSequence.visibility = View.GONE
            activitySupportReportBinding.tvVoidAmount.visibility = View.GONE

            activitySupportReportBinding.tvRefundLabel.visibility = View.GONE
            activitySupportReportBinding.tvRefundSequence.visibility = View.GONE
            activitySupportReportBinding.tvRefundAmount.visibility = View.GONE
        }

        from = bundle?.getString(Constants.FROM)
        to = bundle?.getString(Constants.TO)

        activitySupportReportBinding.tvStoreName.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()
        activitySupportReportBinding.tvMID.text = sharedPreferenceUtil.getMerchantID()
        activitySupportReportBinding.tvTerminalNo.text = sharedPreferenceUtil.getTerminalID()
        activitySupportReportBinding.tvTime.text =
            bundle?.getString(Constants.FROM_DATE) + " to " + bundle?.getString(Constants.TO_DATE)

        val summaryReportViewModel =
            ViewModelProvider(
                this,
                SummaryReportFactory(this)
            )[SummaryReportViewModel::class.java]

        val summaryDates = SummaryDates(to!!, from!!)
        val summaryReportRequest =
            SummaryReportRequest(channel, summaryDates, sharedPreferenceUtil.getTerminalID(),
                sharedPreferenceUtil.getGatewayMerchantID(),sharedPreferenceUtil.getMerchantID())

        val dataObject = JSONObject()
        summaryReportViewModel.getSummary(this, summaryReportRequest, sharedPreferenceUtil)
        summaryReportViewModel.getData().observeOnce(this@SummaryReportActivity) {
            if (it?.data != null) {

                val datesObject = JSONObject()
                datesObject.put("from", from)
                datesObject.put("to", to)

                dataObject.put("dates", datesObject)
                dataObject.put("saleAmount", it.data.saleAmount)
                dataObject.put("saleCount", it.data.saleCount)
                dataObject.put("refundAmount", it.data.refundAmount)
                dataObject.put("refundCount", it.data.refundCount)
                dataObject.put("voidAmount", it.data.voidAmount)
                dataObject.put("voidCount", it.data.voidCount)
                dataObject.put("totalAmount", it.data.totalAmount)
                dataObject.put("totalcount", it.data.totalcount)
                dataObject.put("servCharges", it.data.servCharges)
                if (channel != null) {
                    dataObject.put("channel", channel)
                }
                dataObject.put("tid", sharedPreferenceUtil.getTerminalID())
                dataObject.put("mid", sharedPreferenceUtil.getMerchantID())
                dataObject.put("gatewayMid", sharedPreferenceUtil.getGatewayMerchantID())

                if (sharedPreferenceUtil.getCataLogAmount()) {
                    dataObject.put("serviceCatalogue", "on")
                } else {
                    dataObject.put("serviceCatalogue", "off")
                }

                activitySupportReportBinding.tvSaleSequence.text = it.data.saleCount
                if (it.data.saleAmount != null && it.data.saleAmount != "0") {
                    activitySupportReportBinding.tvSaleAmount.text = ContextUtils.formatWithCommas(it.data.saleAmount.toDouble())
                } else {
                    activitySupportReportBinding.tvSaleAmount.text = "0.00"
                }
                activitySupportReportBinding.tvVoidSequence.text = it.data.voidCount
                if (it.data.voidAmount != "0") {
                    activitySupportReportBinding.tvVoidAmount.text = "-" + it.data.voidAmount
                } else {
                    activitySupportReportBinding.tvVoidAmount.text = "0.00"//it.data.voidAmount
                }
                activitySupportReportBinding.tvRefundSequence.text = it.data.refundCount
                if (it.data.refundAmount != "0") {
                    activitySupportReportBinding.tvRefundAmount.text = "-" + it.data.refundAmount
                } else {
                    activitySupportReportBinding.tvRefundAmount.text = "0.00"//it.data.refundAmount
                }
                if (it.data.totalcount != null && it.data.totalcount != "0") {
                    activitySupportReportBinding.tvTOTSequence.text = it.data.totalcount
                } else {
                    activitySupportReportBinding.tvTOTSequence.text = "0"
                }
                if (it.data.totalAmount != null && it.data.totalAmount != "0") {
                    activitySupportReportBinding.tvTOTAmount.text = ContextUtils.formatWithCommas(it.data.totalAmount.toDouble())
                } else {
                    activitySupportReportBinding.tvTOTAmount.text = "0.00"
                }
            }
        }

        activitySupportReportBinding.btnMerchantCopy.setOnClickListener {
         ring?.start()
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.share_bottom_sheet, null)
            bottomSheetDialog.setCancelable(true)
            bottomSheetDialog.setContentView(view)
            val ivProceed = view.findViewById<Button>(R.id.ivProceed)
            val tvShareOptionTitle = view.findViewById<TextView>(R.id.tvShareOptionTitle)
            val etWhatsApp = view.findViewById<EditText>(R.id.etWhatsApp)
            val ivWhatsApp = view.findViewById<ImageView>(R.id.ivWhatsApp)
            val ivEmail = view.findViewById<ImageView>(R.id.ivEmail)
            val ivSMS = view.findViewById<ImageView>(R.id.ivSMS)
            val btnHome = view.findViewById<Button>(R.id.btnHome)
            val etEmail = view.findViewById<EditText>(R.id.etEmail)
            val clWhatapp = view.findViewById<ConstraintLayout>(R.id.clWhatsapp)
            val ivPrint = view.findViewById<ImageView>(R.id.ivPrint)
            val ivQRCode = view.findViewById<ImageView>(R.id.ivQRCode)


            val bundle = intent.extras
            val titlesArray = resources.getStringArray(R.array.modeType)
            // Create an ArrayAdapter using a simple spinner layout and languages array
            val titlesAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item,
                titlesArray
            )
            // Set layout to use when the list of choices appear
            titlesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Set Adapter to Spinner
            val ivBack = view.findViewById<ImageView>(R.id.ivBackBtn)
            ivBack.setOnClickListener {
                ring?.start()
                bottomSheetDialog.dismiss()
            }

            btnHome.setOnClickListener {
                ring?.start()
                startActivity(
                    Intent(
                        this,
                        DashboardActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }

            ivProceed.setOnClickListener() {
                ring?.start()
                if (validateInputFields()) {
                    if (optionSelected == "Email") {
                        if (etEmail.text?.isNotEmpty() == true) {
                            val base64Str = ContextUtils.getBase64String(dataObject.toString())
                            val encodedStr =
                                URLEncoder.encode(base64Str, StandardCharsets.UTF_8.toString())
                            val finalURL =
                                url + encodedStr
                            val sendURLRequest =
                                SendURLRequest(
                                    "PayRow Receipt",
                                    etEmail.text.toString(),
                                    finalURL, null
                                )
                            summaryReportViewModel?.sendURLDetails(
                                sendURLRequest
                            )
                            summaryReportViewModel?.sendURLLiveData()
                                ?.observeOnce(this@SummaryReportActivity) {
                                    bottomSheetDialog.dismiss()
                                    startActivity(
                                        Intent(
                                            this,
                                            CustomerCopySharedActivity::class.java
                                        )
                                    )
                                }

                        } else {
                            Toast.makeText(
                                this,
                                "Please enter email id to proceed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Please select an option to proceed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            ivEmail.setOnClickListener {
                clWhatapp.visibility = View.GONE
                etEmail.visibility = View.VISIBLE
                optionSelected = "Email"
                ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_default)
                ivEmail.setImageResource(R.drawable.ic_icon_email_active)
                ivSMS.setImageResource(R.drawable.ic_icon_email_default)
                tvShareOptionTitle.text = "Email"
            }

            ivPrint.setOnClickListener {

                val rootView = this.window.decorView.rootView
                rootView.isDrawingCacheEnabled = true
                val bitmapData = Bitmap.createBitmap(rootView.drawingCache)

            }

            ivQRCode.setOnClickListener {
                ring?.start()
                val base64Str = ContextUtils.getBase64String(dataObject.toString())
                val encodedStr =
                    URLEncoder.encode(base64Str, StandardCharsets.UTF_8.toString())
                val finalURL = url + encodedStr
                startActivity(
                    Intent(
                        this,
                        QRCodeReceiptActivity::class.java
                    ).putExtra("InvoiceURL", finalURL)
                )
            }

            bottomSheetDialog.show()
        }

        activitySupportReportBinding.btnHome.setOnClickListener {
            startActivity(
                Intent(this, DashboardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }
    }

    private fun validateInputFields(): Boolean {
        if (optionSelected.isNotEmpty()
        ) {
            return true
        }
        return false
    }
}