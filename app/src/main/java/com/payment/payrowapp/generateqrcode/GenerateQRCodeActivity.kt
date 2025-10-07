package com.payment.payrowapp.generateqrcode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityForgotTidsuccessfullBinding
import com.payment.payrowapp.databinding.ActivityGenerateQrcodeBinding
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.dialogs.QRCodeSessionDialog
import com.payment.payrowapp.dialogs.QRCodeSessionListener
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivityViewModel
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivityViewModelFactory
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.refundandreversal.RefundViewModel
import com.payment.payrowapp.refundandreversal.RefundViewModelFactory
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.concurrent.TimeUnit


class GenerateQRCodeActivity : BaseActivity(),QRCodeSessionListener {

    private  var orderNumber: String?=null
    lateinit var amount: String
    var decodedByte: Bitmap? = null
    var optionSelected = "WhatsApp"
    lateinit var share: Intent
    var shareUrl: String? = null
    var toggleExpire: Boolean? = null
    private val logoutTime = (7 * 60 * 1000).toLong()
    private var sseConnection: ServerSentEvent? = null
    private var streamUrl =
        ApiClient.BASE_URL_SOFT_POS +"/gateway/payrow/stream?checkoutId="
    var serviceList = ArrayList<Service>()
    var payRowVATStatus = false
    var payRowVATAmount = 0.0F
    private var pureBase64Encoded: String? = null
    private lateinit var binding: ActivityGenerateQrcodeBinding
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private lateinit var generateQRCodeViewModel: GenerateQRCodeViewModel

    private var customerBillingPostalCode: String? = null
    private var customerBillingState: String? = null
    private var customerBillingCity: String? = null
    private var customerBillingCountry: String? = null
    private var customerName: String? = null
    private var purchaseBreakdown: String? = null
    private var merchantEmail: String? = null
    private var customerEmail: String? = null
    private var customerPhone: String? = null
    private var bankTransferURL: String? = null
    private var merchantSiteUrl: String? = null
    private var urn: String? = null
    private var checkoutId: String? = null
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_generate_qrcode)
        binding = ActivityGenerateQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
       // setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Pay by QR code"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        amount = bundle?.getString(Constants.TOTAL_AMOUNT)!!
        toggleExpire = bundle.getBoolean("toggleExpire")

        payRowVATStatus = bundle.getBoolean("VatStatus")
        payRowVATAmount = bundle.getFloat("VatAmount")

        if (bundle.get("purchaseBreakDown") != null) {
            purchaseBreakdown = bundle.getString("purchaseBreakDown")

            // orderNumber = bundle.getString("OrderNumber")
            merchantEmail = bundle.getString("merchantEmail")

            customerPhone = bundle.getString("customerPhone")
            customerEmail = bundle.getString("customerEmail")
            bankTransferURL = bundle.getString("MerchantBankURL")

            customerName = bundle.getString("customerName")
            customerBillingCountry = bundle.getString("customerBillingCountry")
            customerBillingCity = bundle.getString("customerBillingCity")
            customerBillingState = bundle.getString("customerBillingState")
            customerBillingPostalCode = bundle.getString("customerBillingPostalCode")

            urn = bundle.getString("urn")
            merchantSiteUrl = bundle.getString("merchantSiteUrl")
            checkoutId = bundle.getString("checkoutId")
        }

        binding.btnHome.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(this, DashboardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }

        binding.buttonCancel.setOnClickListener {
            ring?.start()
            QRCodeSessionDialog(this,this).show()
        }
        // share functionality
        binding.btnMerchantCopy.setOnClickListener {
            ring?.start()
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.share_bottom_sheet, null)
            bottomSheetDialog.setCancelable(true)
            bottomSheetDialog.setContentView(view)

            val ivProceed = view.findViewById<Button>(R.id.ivProceed)
            val etWhatsApp = view.findViewById<EditText>(R.id.etWhatsApp)
            val ivWhatsApp = view.findViewById<ImageView>(R.id.ivWhatsApp)
            val ivEmail = view.findViewById<ImageView>(R.id.ivEmail)
            val ivSMS = view.findViewById<ImageView>(R.id.ivSMS)
            val tvShareOptionTitle =
                view.findViewById<TextView>(R.id.tvShareOptionTitle)
            val btnHome = view.findViewById<Button>(R.id.btnHome)
            val etEmail = view.findViewById<EditText>(R.id.etEmail)
            val clWhatapp = view.findViewById<ConstraintLayout>(R.id.clWhatsapp)
            val ivQRCode = view.findViewById<ImageView>(R.id.ivQRCode)

            ivQRCode.visibility = View.GONE

            optionSelected = "Email"
            clWhatapp.visibility = View.GONE
            etEmail.visibility = View.VISIBLE
            ivWhatsApp.visibility = View.VISIBLE
            ivEmail.visibility = View.GONE
            //ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_default)
            ivWhatsApp.setImageResource(R.drawable.ic_icon_email_active)
            ivSMS.setImageResource(R.drawable.ic_icon_sms_default)
            tvShareOptionTitle.text = "Email"

            /* ivWhatsApp.setOnClickListener {
                 val bundle1 = Bundle()
                 bundle1.putString("TYPE", "")
                 optionSelected = "WhatsApp"
                 clWhatapp.visibility = View.VISIBLE
                 etEmail.visibility = View.GONE
                 ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_active)
                 ivEmail.setImageResource(R.drawable.ic_icon_email_default)
                 ivSMS.setImageResource(R.drawable.ic_icon_sms_default)
                 tvShareOptionTitle.text = "WhatsApp Number"
                 etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
             }
             ivEmail.setOnClickListener {
                 optionSelected = "Email"
                 clWhatapp.visibility = View.GONE
                 etEmail.visibility = View.VISIBLE
                 ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_default)
                 ivEmail.setImageResource(R.drawable.ic_icon_email_active)
                 ivSMS.setImageResource(R.drawable.ic_icon_sms_default)
                 tvShareOptionTitle.text = "Email"
             }*/

            btnHome.setOnClickListener {
                ring?.start()
                startActivity(
                    Intent(this, DashboardActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                )
            }

            ivProceed.setOnClickListener {
                ring?.start()
                /*ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    0
                )
                val read_permission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                val write_permission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )*/

              //  if (read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED) {
                    bottomSheetDialog.dismiss()
                    when (optionSelected) {
                        "WhatsApp" -> shareQRCODE(
                            optionSelected,
                            etWhatsApp.text.toString(),
                            "WhatsApp not installed in this device."
                        )
                        "Email" -> sendQRCode(etEmail.text.toString())
                        /*shareQRCODE(
                                optionSelected,
                                etEmail.text.toString(),
                                "There is no application to support this action"
                            )*/

                    }
              /*  } else {
                    Toast.makeText(
                        this,
                        "Permission denied.Please provide permissions.",
                        Toast.LENGTH_SHORT
                    ).show()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        0
                    )
                }*/
                /* startActivity(
                     Intent(this, PaymentLinkSuccessScreenActivity::class.java).addFlags(
                         Intent.FLAG_ACTIVITY_CLEAR_TOP
                     ).putExtra("Type", "GENERATE QR")
                 )*/
            }
            bottomSheetDialog.show()
        }

        sharedPreferenceUtil = SharedPreferenceUtil(this)
        // generate QR request
         generateQRCodeViewModel =
            ViewModelProvider(
                this,
                GenerateQRCodeViewModelFactory(this)
            ).get(GenerateQRCodeViewModel::class.java)

     //   if (amount.toInt() > 0) {


        if (purchaseBreakdown != null) {
            serviceList = SharedData.sharedArrayList
        } else if (sharedPreferenceUtil.getCataLogAmount()) {
            serviceList = SharedData.sharedArrayList
            // SharedData.sharedArrayList = arrayListOf()
        } else {
            SharedData.sharedArrayList[0].transactionAmount =
                (amount.toFloat() + payRowVATAmount).toDouble()
            serviceList = SharedData.sharedArrayList
            // SharedData.sharedArrayList = arrayListOf()
        }
        val purchaseDetails = PurchaseDetail(serviceList)
        val paymentMethodList =
            arrayOf(Constants.EDIRHAM_CARD, Constants.NON_EDIRHAM_CARD)
        //   val randomNumber = (10000..99999).shuffled().last()

        orderNumber = if (bundle.getString("OrderNumber") != null) {
            bundle.getString("OrderNumber")
            //  sharedPreferenceUtil.setOrderNum(orderNumber)
        } else {
            ContextUtils.randomValue()
                .toString() + ContextUtils.getRandomLastValue()//randomNumber.toString()
        }
        streamUrl += "$orderNumber"

        if (customerBillingState.isNullOrEmpty()) {
            customerBillingState = sharedPreferenceUtil.getAddress()
        }

        if (urn.isNullOrEmpty()) {
            urn = sharedPreferenceUtil.getURN()//Constants.URN
        }

        if (merchantSiteUrl.isNullOrEmpty()) {
            merchantSiteUrl = ApiClient.MERCHANT_BANK_TRANS_URL
        }

        if (bankTransferURL.isNullOrEmpty()) {
            bankTransferURL = ApiClient.MERCHANT_BANK_TRANS_URL
        }


        if (customerName.isNullOrEmpty()) {
            customerName = sharedPreferenceUtil.getMerchantName()
        }

        if (customerBillingCity.isNullOrEmpty()) {
            customerBillingCity = sharedPreferenceUtil.getCity()
        }

        if (customerBillingCountry.isNullOrEmpty()) {
            customerBillingCountry = sharedPreferenceUtil.getCountry()
        }

        if (customerBillingPostalCode.isNullOrEmpty()) {
            customerBillingPostalCode = sharedPreferenceUtil.getBOBox()
        }

        if (customerEmail.isNullOrEmpty()) {
            customerEmail = sharedPreferenceUtil.getMailID()
        }


        if (customerPhone.isNullOrEmpty()) {
            customerPhone =
                sharedPreferenceUtil.getMerchantMobileNumber()
        }
            val generateQRRequest = GenerateQRRequest(
                null/*sharedPreferenceUtil.getPayByLinkID()*/,
                requireNotNull(orderNumber),
                sharedPreferenceUtil.getAddress(),
                sharedPreferenceUtil.getAddress(),
                "EN",
                Constants.GENERATE_QR,
                true,
                true,
                merchantSiteUrl!!,
                bankTransferURL!!,
                paymentMethodList,
                Constants.SESSIONS_TIMEOUT_SEC,
                sharedPreferenceUtil.getMerchantName(),
                sharedPreferenceUtil.getURN(),
                Constants.EDIRHAM_CARD,
                "Pending",
                sharedPreferenceUtil.getMailID(),
                sharedPreferenceUtil.getMerchantMobileNumber(),
                sharedPreferenceUtil.getCity(),
                sharedPreferenceUtil.getCity(),
                sharedPreferenceUtil.getCountry(),
                sharedPreferenceUtil.getBOBox(),
                toggleExpire!!,
                purchaseDetails, amount.toFloat(), sharedPreferenceUtil.getTerminalID(),
                sharedPreferenceUtil.getDistributorID(), sharedPreferenceUtil.getMailID(),
                sharedPreferenceUtil.getMerchantMobileNumber(),
                sharedPreferenceUtil.getBussinessId(), "Pending",
                sharedPreferenceUtil.getMerchantEmail(), sharedPreferenceUtil.getMerchantPhone(),
                sharedPreferenceUtil.getReportID(),
                HeaderSignatureUtil.getDeviceSN(), payRowVATStatus, payRowVATAmount,sharedPreferenceUtil.getGatewayMerchantID(),
            checkoutId)

            generateQRCodeViewModel.getQRResponse(generateQRRequest)
      //  }

        generateQRCodeViewModel.getData().observeOnce(this) {
            if (it?.qrCodeURL != null) {
                shareUrl = it.qrCodeURL.substring((it.qrCodeURL.indexOf(",") + 1))
                pureBase64Encoded =
                    it.qrCodeURL.substring((it.qrCodeURL.indexOf(",") + 1))
                val decodedString: ByteArray =
                    Base64.decode(pureBase64Encoded, Base64.DEFAULT)
                decodedByte =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                binding.payQRImage.setImageBitmap(decodedByte)

                if (!toggleExpire!!) {
                    binding.btnMerchantCopy.visibility = View.GONE
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.clSecondsLeft.visibility = View.VISIBLE
                    startUserSession()
                    callBidStreamAPI(sharedPreferenceUtil.getAuthToken())
                }
            }
        }
    }

    private fun sendQRCode(email: String) {
        val paymentSuccessfulActivityViewModel =
            ViewModelProvider(
                this,
                PaymentSuccessfulActivityViewModelFactory(this)
            ).get(PaymentSuccessfulActivityViewModel::class.java)


        val sendURLRequest =
            SendURLRequest(
                "PayRow QRCode",
                email,
                pureBase64Encoded, "qrcode"
            )
        paymentSuccessfulActivityViewModel.sendURLDetails(
            sendURLRequest
        )
        paymentSuccessfulActivityViewModel?.sendURLLiveData()
            ?.observeOnce(this@GenerateQRCodeActivity) {

            }
    }

    private fun callBidStreamAPI(authToken: String) {
        Log.e(
            "LiveEvent",
            streamUrl
        )
        val request: Request = Request.Builder()
            .url(streamUrl)
            .addHeader("Authorization", "Bearer $authToken")
            .build()

        val oksse = OkSse()
        sseConnection = oksse.newServerSentEvent(request, object : ServerSentEvent.Listener {
            override fun onOpen(sse: ServerSentEvent?, response: Response?) {
                Log.e("LiveEvent", "onOpen")
            }

            override fun onMessage(
                sse: ServerSentEvent?,
                id: String?,
                event: String?,
                message: String?
            ) {
                Log.e("LiveEvent", message!!)
                var status = message.replace("\"", "")
                if (status.equals("NOT APPROVED", ignoreCase = true) || status.equals(
                        "PRESENTED",
                        ignoreCase = true
                    ) || status.equals(
                        "DENIED BY RISK",
                        ignoreCase = true
                    ) || status.equals(
                        "HOST TIMEOUT",
                        ignoreCase = true
                    )  || status.equals("CLOSED", ignoreCase = true) || status.equals(
                        "CANCELED",
                        ignoreCase = true
                    ) || status.equals("NOT CAPTURED", ignoreCase = true)
                ) {
                    sseConnection?.close()
                    cancelTimer()
                    getEnquiryStatus()
                   // getPaymentStatus()
                } else if (status.equals("APPROVED", ignoreCase = true) || status.equals(
                        "CAPTURED",
                        ignoreCase = true)) {
                    sseConnection?.close()
                    cancelTimer()
                    getPaymentStatus()
                }
            }

            override fun onComment(sse: ServerSentEvent?, comment: String?) {
                Log.e("LiveEvent", "onComment")
            }


            override fun onRetryTime(sse: ServerSentEvent?, milliseconds: Long): Boolean {
                Log.e("LiveEvent", "onRetryTime")
                return true
            }


            override fun onRetryError(
                sse: ServerSentEvent?,
                throwable: Throwable?,
                response: Response?
            ): Boolean {
                Log.e("LiveEvent", "--onRetryError--" + response?.message.toString())
                return true
            }


            override fun onClosed(sse: ServerSentEvent?) {
                Log.e("LiveEvent", "--onClosed==")
            }


            override fun onPreRetry(sse: ServerSentEvent?, originalRequest: Request?): Request? {
                Log.e("LiveEvent", "--onPreRetry--")

                return request // Don't chage this, this will re-open the channel
            }


        })

    }

    private fun startUserSession() {
        cancelTimer()
        timer = object : CountDownTimer(logoutTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                try {
                    // val secsLeft: Int = (millisUntilFinished / 1000).toInt()
                    binding.textViewTimeoutSeconds.text = String.format(
                        "0%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFinish() {
               // getPaymentStatus()
                getEnquiryStatus()
            }
        }
        timer?.start()
    }

    private fun getEnquiryStatus() {
        runOnUiThread {
            orderNumber?.let {
                val enquiryRequestClass =
                    EnquiryRequestClass(it, sharedPreferenceUtil.getGatewayMerchantID())
                generateQRCodeViewModel.getEnquiryResponse(enquiryRequestClass) { result->
                    showToast(getString(R.string.something_went_wrong_kindly_check_payement_status))
                    finish()
                }
            }
            generateQRCodeViewModel.getEnquiryData()
                .observeOnce(this@GenerateQRCodeActivity) {
                    if (it.data!=null&&!it.data?.checkoutStatus.isNullOrEmpty()) {
                        val intent =
                            Intent(
                                this@GenerateQRCodeActivity,
                                QRCodeConfirmationActivity::class.java
                            )
                        intent.putExtra("orderNumber", orderNumber)
                        intent.putExtra("status", it.data?.checkoutStatus)
                        intent.putExtra("Amount", amount)

                        if (!it.data?.cardNumber.isNullOrEmpty()) {
                            intent.putExtra("cardNumber", it.data?.cardNumber)
                        }

                        if (!it.data?.cardBrand.isNullOrEmpty()) {
                            intent.putExtra("cardBrand", it.data?.cardBrand)
                        }

                        intent.putExtra("payRowVATStatus", payRowVATStatus)
                        intent.putExtra("payRowVATAmount", payRowVATAmount)
                        if (it.data?.amount != null) {
                            intent.putExtra("totalAmount", it.data?.amount.toString())
                        }
                        intent.putExtra("channel", "generateQR")

                        if (!it.data?.auth.isNullOrEmpty()) {
                            intent.putExtra("auth", it.data?.auth)
                        }

                        startActivity(intent)
                        finish()
                    } else {
                        showToast(getString(R.string.something_went_wrong_kindly_check_payement_status))
                        finish()
                    }
                }
        }
    }

    private fun getPaymentStatus() {
        runOnUiThread {
            val refundViewModel =
                ViewModelProvider(
                    this,
                    RefundViewModelFactory(this)
                ).get(RefundViewModel::class.java)
            refundViewModel.getQrCodeInvoice(orderNumber!!)
            refundViewModel.getQrCodeInvoiceLiveData()
                .observeOnce(this@GenerateQRCodeActivity) {
                    if (it.data.get(0).purchaseBreakdown.service.size > 0) {
                        val intent =
                            Intent(
                                this@GenerateQRCodeActivity,
                                QRCodeConfirmationActivity::class.java
                            )
                        intent.putExtra("orderNumber", it.data.get(0).orderNumber)
                        intent.putExtra("status", it.data[0].orderStatus)
                        intent.putExtra("Amount", amount)
                        intent.putExtra("cardNumber", it.data.get(0).cardNumber)
                        intent.putExtra("cardBrand", it.data.get(0).cardBrand)
                        intent.putExtra("payRowVATStatus", payRowVATStatus)
                        intent.putExtra("payRowVATAmount", payRowVATAmount)
                        /* intent.putExtra("PayRowCharge", it.data.get(0).purchaseBreakdown.fee.get(0).feeAmount)
                         intent.putExtra("other", it.data.get(0).purchaseBreakdown.fee.get(1).feeAmount)
                         intent.putExtra("bankFee", it.data.get(0).purchaseBreakdown.fee.get(2).feeAmount)*/
                        intent.putExtra("totalAmount", it.data.get(0).amount)
                        intent.putExtra("channel", "generateQR")
                        intent.putExtra("auth", it.data.get(0).auth)

                        startActivity(intent)
                        finish()
                    }
                }
        }
    }

    private fun shareQRCODE(
        optionSelected: String,
        inputFiled: String,
        description: String
    ) {
        try {
            val bytes = ByteArrayOutputStream()
            decodedByte!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

            val path: String = MediaStore.Images.Media.insertImage(
                contentResolver,
                decodedByte,
                "QR Code",
                null
            )

            // Now send it out to share
            if (optionSelected == "WhatsApp") {
                share = Intent(Intent.ACTION_SEND)
                share.setPackage("com.whatsapp")
                share.type = "image/*"
                share.putExtra(Intent.EXTRA_TEXT, "PayRow QRCode")
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (optionSelected == "Email") {
                val emailArray = arrayOf(inputFiled)
                share = Intent(Intent.ACTION_SEND)
                share.type = "message/rfc822"
                share.putExtra(Intent.EXTRA_EMAIL, emailArray)
                share.putExtra(Intent.EXTRA_SUBJECT, "PayRow QRCode")
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
            }

            if (share.resolveActivity(packageManager) != null) {
                startActivity(share)
            } else {
                showToast(description)
            }
        } catch (e: Exception) {
            // need to handle
            showToast(e.message.toString())
        }
    }

    private fun cancelTimer() {
        if (timer != null)
            timer?.cancel()
    }

    companion object {
        private var timer: CountDownTimer? = null
    }

    override fun onDestroy() {
        super.onDestroy()

        if (sseConnection != null) {
            Log.e("LiveEvent", "--onDestroy--AuctionDetailFragment")
            sseConnection?.close()
        }
        SharedData.sharedArrayList = arrayListOf()
        orderNumber = ""
    }

    override fun closeSession() {
        if (sseConnection != null) {
            Log.e("LiveEvent", "--onDestroy--AuctionDetailFragment")
            sseConnection?.close()

            startActivity(
                Intent(this, DashboardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }
    }

}