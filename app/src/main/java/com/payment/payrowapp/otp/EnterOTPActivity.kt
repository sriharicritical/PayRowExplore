package com.payment.payrowapp.otp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityEnterOtpBinding
import com.payment.payrowapp.databinding.ActivityLoginNewBinding
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.generateqrcode.QRCodeConfirmationActivity
import com.payment.payrowapp.login.AuthViewModelFactory
import com.payment.payrowapp.login.AuthenticationRepository
import com.payment.payrowapp.login.AuthenticationViewModel
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.refundandreversal.RefundConfirmationActivity
import com.payment.payrowapp.refundandreversal.RefundPaymentActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


class EnterOTPActivity : BaseActivity() {
    private var purchaseNumber: String? = null
    private var refundId: String? = null
    private var cardNumber: String? = null
    private var mode: String? = null
    private var totalAmount: String? = null
    private var orderNumber: String? = null
    var ring: MediaPlayer? = null
    lateinit var timer: CountDownTimer
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    var purchaseRequest: PurchaseRequest? = null
    private var refId32: String? = null
    private var refId33: String? = null
    private var refId7: String? = null
    private var refId11: String? = null
    private var refId12: String? = null
    private var refId13: String? = null
    private var refId37: String? = null
    private var responseCode: String? = null
    private var authorizationId: String? = null
    private var base64: String? = null

    private var hostRefNO: String? = null
    private var vasRefNO: String? = null
    private var purchaseAmount: String? = null
    private var cardBrand: String? = null
    private var track2Data: String? = null
    private var posEntryMode: String? = null
    private var channel: String? = null
    private var ICCData: String? = null
    private var expiryDate: String? = null
    private var cardsequencenumber: String? = null
    private var transactionType: Int? = null
    private var referenceId48: String? = null
    private var pmtTxnRefCode: String? = null
    private lateinit var binding: ActivityEnterOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_enter_otp)
        binding = ActivityEnterOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ivProceed: ImageView = findViewById(R.id.ivProceed)
        val etOtp1: EditText = findViewById(R.id.etOtp1)

        //  setSupportActionBar(myToolbar)
        setupToolbar()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "OTP"
        sharedPreferenceUtil = SharedPreferenceUtil(this)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras

        if (bundle != null) {
            channel = bundle.getString("channel")

            if (channel != "toggle") {
                orderNumber = bundle.getString("OrderNumber")
                mode = bundle.getString("mode")

                if (channel == "Partial") {
                    hostRefNO = bundle.getString("hostRefNO")
                    vasRefNO = bundle.getString("vasRefNO")
                    cardNumber = bundle.getString("cardNumber")
                    purchaseRequest = bundle.get("PurchaseRequest") as PurchaseRequest
                    cardBrand = bundle.getString("cardType")
                    totalAmount = bundle.getString("totalAmount")
                    referenceId48 = bundle.getString("referenceId48")
                }

                if (channel == Constants.CARD) {
                    purchaseNumber = bundle.getString("purchaseNumber")
                    totalAmount = bundle.getString("totalAmount")
                    cardNumber = bundle.getString("cardNumber")
                    refundId = bundle.getString("refundId")
                    refId7 = bundle.getString("refId7")
                    refId11 = bundle.getString("refId11")
                    refId13 = bundle.getString("refId13")
                    refId12 = bundle.getString("refId12")
                    refId32 = bundle.getString("refId32")
                    refId33 = bundle.getString("refId33")
                    refId37 = bundle.getString("refId37")
                    responseCode = bundle.getString("responseCode")
                    authorizationId = bundle.getString("authorizationId")
                    base64 = bundle.getString("base64")
                    hostRefNO = bundle.getString("hostRefNO")
                    vasRefNO = bundle.getString("vasRefNO")
                    purchaseAmount = bundle.getString("purchaseAmount")
                    cardBrand = bundle.getString("cardType")

                    track2Data = bundle.getString("track2Data")
                    posEntryMode = bundle.getString("posEntryMode")
                    ICCData = bundle.getString("ICCData")
                    expiryDate = bundle.getString("expiryDate")
                    cardsequencenumber = bundle.getString("cardsequencenumber")
                    transactionType = bundle.getInt("TRANSACTION_TYPE")
                    referenceId48 = bundle.getString("referenceId48")
                    pmtTxnRefCode = bundle.getString("pmtTxnRefCode")
                    //  sharedPreferenceUtil.setAmount(totalAmount)
                }
            }
        }

        //   getOTPViewModel.getOTPResponse(sendSMS)

        /*getOTPViewModel.getData().observe(this, object : Observer<OTPResponse> {
            override fun onChanged(t: OTPResponse?) {
                Log.d("Response", "API executed")
            }
        })*/

        authenticationViewModel =
            ViewModelProvider(
                this,
                AuthViewModelFactory(this)
            ).get(AuthenticationViewModel::class.java)

        binding.btnResendCode.setOnClickListener {
            ring?.start()
            if (this::timer.isInitialized) {
                timer.cancel()
            }
            startTimer()
            binding.tvResendCode.visibility = View.VISIBLE
            binding.tvResendCode.text = "Resend OTP"
            val tid =
                sharedPreferenceUtil.getTerminalID()//preference!!.getString(Constants.MERCHANT_ID, "")

            if (channel == "toggle") {
                val merchantID = sharedPreferenceUtil.getMerchantID()
                val otpMIDRequest = merchantID.let {
                    OTPRequest(
                        null, it
                    )
                }
                getMIDOtp(otpMIDRequest)
            } else {
                val otpRequest = tid.let {
                    OTPRequest(
                        it, null
                    )
                }
                getOTP(otpRequest)
            }
        }
        binding.btnContinue.setOnClickListener {
            ring?.start()

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.show()

            if (channel == "toggle") {
                val verifyOTPRequest = VerifyOTPRequest(
                    binding.etOtp.value,
                    null,
                    sharedPreferenceUtil.getMerchantID(),
                    ContextUtils.randomValue().toString()
                )
                bundle?.getString("service")?.let { it1 ->
                    authenticationViewModel.getMIDOTPVerifyResponse(
                        it1, bundle.getBoolean("status"), verifyOTPRequest
                    )
                }
                authenticationViewModel.getMIDVerifyData().removeObservers(this)
                authenticationViewModel.getMIDVerifyData()
                    .observeOnce(this@EnterOTPActivity) {
                        progressDialog.dismiss()
                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
            } else {
                val verifyOTPRequest = VerifyOTPRequest(
                    binding.etOtp.value,
                    sharedPreferenceUtil.getTerminalID(),
                    null,
                    ContextUtils.randomValue().toString()
                )
                authenticationViewModel.verifyOTPResponse(verifyOTPRequest)
                authenticationViewModel.getVerifyOTPData()
                    .observeOnce(this@EnterOTPActivity) {

                        if (channel == "toggle") {
                            val resultIntent = Intent()
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        } else {
                            if (channel == "Partial") {
                                authenticationViewModel.sendPurchaseRequest(
                                    cardNumber!!, vasRefNO!!, mode!!, orderNumber!!,
                                    this@EnterOTPActivity,
                                    purchaseRequest!!
                                )

                                authenticationViewModel.purchaseResponse()
                                    .observeOnce(this@EnterOTPActivity) {
                                        progressDialog.dismiss()

                                        val purchaseFeeRequest =
                                            preparePartialFeeReq(purchaseRequest!!)
                                        val ackPurchaseRequest = prepareACKRequest(
                                            it.purchaseResult.orderNo,
                                            it.purchaseResult.responseCode,
                                            it.purchaseResult.authorizationId,
                                        )

                                        purchaseFeeRequest.fssFetch = ackPurchaseRequest
                                        purchaseFeeRequest.posConditionCode = "52"
                                        purchaseFeeRequest.processingCode = "490000"
                                        // purchaseFeeRequest?.currencyCode = "784"
                                        purchaseFeeRequest.ackCardBrand = cardBrand
                                        AuthenticationRepository.getFSSFeeAPI(
                                            this,
                                            purchaseFeeRequest, false
                                        )
                                        val intent =
                                            Intent(this, RefundConfirmationActivity::class.java)
                                        if (it.purchaseResult.responseCode == "00") {
                                            intent.putExtra(
                                                "authCode",
                                                it.purchaseResult.authorizationId
                                            )
                                            intent.putExtra("status", "VOIDED")
                                        } else {
                                            intent.putExtra("status", "NOT VOIDED")
                                        }
                                        // }
                                        intent.putExtra("hostRefNO", it.purchaseResult.orderNo)
                                        intent.putExtra("vasRefNO", vasRefNO)
                                        intent.putExtra("CARDNO", cardNumber)
                                        intent.putExtra("orderNumber", orderNumber)
                                        intent.putExtra("mode", mode)
                                        intent.putExtra(
                                            "responseCode",
                                            it.purchaseResult.responseCode
                                        )
                                        intent.putExtra("cardBrand", cardBrand)
                                        // intent.putExtra("ICCData", purchaseRequest.fiftyFiveData)

                                        startActivity(
                                            intent
                                        )
                                        finish()
                                    }
                            } else
                                if (channel == Constants.CARD) {
                                    cancelTimer()
                                    if (mode!! == "Refund") {
                                        progressDialog.dismiss()

                                        val refundFeeRequest = orderNumber?.let { it1 ->
                                            RefundFeeRequest(
                                                it1, sharedPreferenceUtil.getMerchantID()
                                            )
                                        }
                                        refundFeeRequest?.let { it1 ->
                                            authenticationViewModel.getRefundFeeResponse(
                                                it1
                                            )
                                        }
                                        authenticationViewModel.getRefundData().observeOnce(this) {
                                            if (it?.data != null) {
                                                val feeFetchStr = feeFetchRequest(
                                                    it.data.servicedata,
                                                    it.data.refundAmount!!
                                                )
                                                // purchaseRequest.setAmount(AuthenticationViewModel.getHexDecimalValue(amount));
                                                val purchaseFeeReq =
                                                    authenticationViewModel.prepareFeeReq(
                                                        it.data.refundAmount,
                                                        purchaseRequest!!,
                                                        feeFetchStr
                                                    )

                                                if (it.data.orderNumber != null && it.data.orderNumber.isNotEmpty()) {
                                                    // intent.putExtra("orderNumber", it.data.orderNumber)
                                                    orderNumber = it.data.orderNumber
                                                }

                                                authenticationViewModel.initiateRefundTrans(
                                                    cardNumber!!,
                                                    vasRefNO!!,
                                                    orderNumber!!,
                                                    cardBrand!!,
                                                    mode!!,
                                                    purchaseRequest!!,
                                                    purchaseFeeReq,
                                                    this
                                                )
                                                /* val intent =
                                                 Intent(this, RefundPaymentActivity::class.java)
                                             intent.putExtra(
                                                 "PurchaseRequest",
                                                 purchaseRequest as Serializable
                                             )
                                             intent.putExtra("cardNumber", cardNumber)
                                             intent.putExtra("vasRefNo", vasRefNO)
                                             intent.putExtra("mode", mode)

                                             if (it.data.orderNumber != null && it.data.orderNumber.isNotEmpty()) {
                                                 intent.putExtra("orderNumber", it.data.orderNumber)
                                             } else {
                                                 intent.putExtra("orderNumber", orderNumber)
                                             }

                                             intent.putExtra("AMOUNT", it.data.refundAmount)
                                             intent.putExtra("cardBrand", cardBrand)
                                             intent.putExtra("ICCData", ICCData)
                                             intent.putExtra("feeFetchStr", feeFetchStr)
                                             startActivity(intent)*/
                                            } else {
                                                if (it?.errorMessage != null) {
                                                    showToast(it.errorMessage)
                                                }
                                                finish()
                                            }
                                        }

                                    } else {
                                        authenticationViewModel.sendPurchaseRequest(
                                            cardNumber!!, vasRefNO!!, mode!!, orderNumber!!,
                                            this@EnterOTPActivity,
                                            purchaseRequest!!
                                        )

                                        authenticationViewModel.purchaseResponse()
                                            .observeOnce(this@EnterOTPActivity) {
                                                progressDialog.dismiss()

                                                val purchaseFeeRequest = prepareFeeReq()
                                                val ackPurchaseRequest = prepareACKRequest(
                                                    it.purchaseResult.orderNo,
                                                    it.purchaseResult.responseCode,
                                                    it.purchaseResult.authorizationId,
                                                )

                                                purchaseFeeRequest.fssFetch = ackPurchaseRequest
                                                purchaseFeeRequest.posConditionCode = "52"
                                                purchaseFeeRequest.processingCode = "490000"
                                                // purchaseFeeRequest?.currencyCode = "784"
                                                purchaseFeeRequest.ackCardBrand = cardBrand
                                                AuthenticationRepository.getFSSFeeAPI(
                                                    this,
                                                    purchaseFeeRequest, false
                                                )

                                                val intent =
                                                    Intent(
                                                        this,
                                                        RefundConfirmationActivity::class.java
                                                    )

                                                if (it.purchaseResult.responseCode == "00") {
                                                    intent.putExtra(
                                                        "authCode",
                                                        it.purchaseResult.authorizationId
                                                    )
                                                    intent.putExtra("status", "VOIDED")
                                                } else {
                                                    intent.putExtra("status", "NOT VOIDED")
                                                }
                                                // }
                                                intent.putExtra(
                                                    "hostRefNO",
                                                    it.purchaseResult.orderNo
                                                )
                                                intent.putExtra("vasRefNO", vasRefNO)
                                                intent.putExtra("CARDNO", cardNumber)
                                                intent.putExtra("orderNumber", orderNumber)
                                                intent.putExtra("mode", mode)
                                                intent.putExtra(
                                                    "responseCode",
                                                    it.purchaseResult.responseCode
                                                )
                                                intent.putExtra("cardBrand", cardBrand)
                                                intent.putExtra("totalAmount", totalAmount)
                                                // intent.putExtra("ICCData", ICCData)

                                                startActivity(
                                                    intent
                                                )
                                                finish()
                                            }
                                    }
                                } else if (channel == "Paybylink" || channel == "generateQR") {

                                    cancelTimer()
                                    if (mode!! == "Refund") {
                                        authenticationViewModel.ecommRefundRequest(
                                            mode!!, orderNumber!!,
                                            this@EnterOTPActivity
                                        )

                                        authenticationViewModel.ecommReffundResp()
                                            .observeOnce(this@EnterOTPActivity) {
                                                progressDialog.dismiss()
                                                if (it?.checkoutStatus != null && it.checkoutStatus.isNotEmpty()) {
                                                    val intent =
                                                        Intent(
                                                            this,
                                                            QRCodeConfirmationActivity::class.java
                                                        )
                                                    intent.putExtra("Amount", it.amount)
                                                    if (it.orderNumber != null && it.orderNumber.isNotEmpty()) {
                                                        intent.putExtra(
                                                            "orderNumber",
                                                            it.orderNumber
                                                        )
                                                    } else {
                                                        intent.putExtra("orderNumber", orderNumber)
                                                    }

                                                    intent.putExtra("status", it.checkoutStatus)
                                                    intent.putExtra("type", channel)
                                                    intent.putExtra("mode", mode)
                                                    startActivity(intent)
                                                }
                                                finish()
                                            }
                                    } else {
                                        authenticationViewModel.ecommVoidRequest(
                                            mode!!, orderNumber!!,
                                            this@EnterOTPActivity
                                        )

                                        authenticationViewModel.ecommVoidResp()
                                            .observeOnce(this@EnterOTPActivity) {
                                                progressDialog.dismiss()
                                                if (it?.checkoutStatus != null && it.checkoutStatus.isNotEmpty()) {
                                                    val intent =
                                                        Intent(
                                                            this,
                                                            QRCodeConfirmationActivity::class.java
                                                        )
                                                    intent.putExtra("Amount", it.amount)
                                                    intent.putExtra("orderNumber", orderNumber)
                                                    intent.putExtra("status", it.checkoutStatus)
                                                    intent.putExtra("type", channel)
                                                    intent.putExtra("mode", mode)
                                                    startActivity(intent)
                                                }
                                                finish()
                                            }
                                    }
                                }
                        }
                    }
            }
        }

        binding.tvResendCode.setOnClickListener {
            //  getOTPViewModel.getOTPResponse(sendSMS)
        }

        /* etOtp.setPinViewEventListener { pinview, fromUser ->
             btnContinue.isEnabled = true
             btnContinue.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
         }*/

        ivProceed.setOnClickListener {
            ring?.start()
            if (etOtp1.text.toString().isNotEmpty()) {
                ring?.start()
                finish()
            } else {
                Toast.makeText(this, "Please enter OTP to proceed", Toast.LENGTH_SHORT).show()
            }
        }

        if (channel != null && channel == Constants.CARD) {
            val decodedString: ByteArray =
                Base64.decode(base64, Base64.DEFAULT)
            val text = String(decodedString, StandardCharsets.UTF_8)
            purchaseRequest = PurchaseRequest(text)
            constructPurchaseRequest(purchaseRequest!!)
        }
    }

    private fun startTimer() {
        // timer running
        timer = object : CountDownTimer(120 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    val secsLeft: Int = (millisUntilFinished / 1000).toInt()
                    //binding.btnResendCode.text = "00:$secsLeft"
                    binding.btnResendCode.text = String.format(
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
//                showToast("Timeout! Please try again")
                cancel()
                finish()
            }
        }
        timer.start()
    }


    private fun getMIDOtp(otpRequest: OTPRequest?) {
        if (otpRequest != null) {
            authenticationViewModel.getMIDOTPResponse(otpRequest)
        }
        authenticationViewModel.getMIDData().observeOnce(this@EnterOTPActivity) {

        }
    }

    private fun getOTP(otpRequest: OTPRequest?) {
        if (otpRequest != null) {
            authenticationViewModel.getOTPResponse(otpRequest)
        }
        authenticationViewModel.getData().observeOnce(this@EnterOTPActivity) {

        }
    }

    private fun constructPurchaseRequest(purchaseRequest: PurchaseRequest): PurchaseRequest {

        if (mode!! == "Refund") {
            purchaseRequest.RefundNum = refundId

            //  purchaseRequest.posPINCaptureCode = "12"//sharedPreferenceUtil.getPOSPinCaptureCode()
            purchaseRequest.MTI = sharedPreferenceUtil.getMIT()
            purchaseRequest.processingCode = "200000"
            purchaseRequest.extendedTXNType = "6200"
            purchaseRequest.terminalOwner = "Default Participant"

            purchaseRequest.time = ContextUtils.getCurrentTime()
            purchaseRequest.date = ContextUtils.getCurrentDate()
            purchaseRequest.transDateAndTime =
                ContextUtils.getCurrentDate() + ContextUtils.getCurrentTime()

            if (transactionType == 4 || transactionType == 2) {
                purchaseRequest.appPANSeqNo = cardsequencenumber
                //   ICCData = updateICCData(ICCData)
                purchaseRequest.fiftyFiveData = ICCData
            }
        } else {
            purchaseRequest.reasonCode = "4000"
            if (responseCode.equals("0") || responseCode.equals("00")) {
                purchaseRequest.responseCode = "00"
            } else {
                purchaseRequest.responseCode = responseCode
            }

            // purchaseRequest.track2Data = track2Data
            purchaseRequest.MTI = sharedPreferenceUtil.getVoidMTI()
            purchaseRequest.field90 =
                sharedPreferenceUtil.getMIT() + refId11 + refId7 + "0000000000" + "000000000000"//refId32+refId33
            purchaseRequest.processingCode = sharedPreferenceUtil.getProcessingCode()
            purchaseRequest.authorizationId = authorizationId
            purchaseRequest.orderNo = refId37


            purchaseRequest.switchKey =
                sharedPreferenceUtil.getTerminalID() + ";" + refId11 + ";" +
                        ContextUtils.getCurrentDate() + ContextUtils.getCurrentTime() + ";420"
            purchaseRequest.originalKey =
                sharedPreferenceUtil.getTerminalID() + ";" + refId11 + ";" + refId7 +
                        ";200"

            purchaseRequest.time = refId12
            purchaseRequest.date = refId13
            purchaseRequest.transDateAndTime = refId7!!

            if (transactionType == 4 || transactionType == 2) {
                purchaseRequest.appPANSeqNo = cardsequencenumber
                purchaseRequest.fiftyFiveData = ICCData
            }
        }


        val decodedString: ByteArray =
            Base64.decode(expiryDate, Base64.DEFAULT)
        val expiryTXT = String(decodedString, StandardCharsets.UTF_8)
        purchaseRequest.expiryDate = expiryTXT


        purchaseRequest.amount =
            purchaseAmount!!//ContextUtils.getStringInHexaDecimal(totalAmount!!)

        purchaseRequest.systemTraceAuditNumber = refId11!!

        purchaseRequest.merchantType =
            sharedPreferenceUtil.getMerchantType()//Constants.MERCHANT_TYPE
        purchaseRequest.posEntryMode = posEntryMode

        purchaseRequest.posConditionCode =
            sharedPreferenceUtil.getPOSConditionCode()//Constants.POS_CONDITION_CODE
        // purchaseRequest.acquirerInstIdCode = refId32!!//sharedPreferenceUtil.getAccInstIdCode()
        purchaseRequest.cardAcceptorTerminalID =
            sharedPreferenceUtil.getTerminalID()
        purchaseRequest.cardAcceptorIDCode = sharedPreferenceUtil.getMerchantID() + "      "
        purchaseRequest.cardAcceptorNameLocation =
            sharedPreferenceUtil.getCardAcceptorNameLocation()
        purchaseRequest.currencyCode =
            sharedPreferenceUtil.getCurrencyCode()//Constants.CURRENCY_CODE
        purchaseRequest.sponsorBank = sharedPreferenceUtil.getSponsorBank()//Constants.SPONSOR_BANK
        return purchaseRequest
    }


    private fun feeFetchRequest(serviceData: ArrayList<FeeServiceData>, txnAmount: String): String {
        val feeObject = JSONObject(referenceId48)
        val fssFeeFetchRequest = FSSFeeFetchReqest(
            "2",
            txnAmount,
            "0900",
            feeObject.getString("correlationId"),
            "MPAY",
            pmtTxnRefCode,
            cardBrand,
            feeObject.getString("merchantTrn"),
            feeObject.getString("serviceTrn"),
            feeObject.getString("refId"),
            serviceData
        )

        return Gson().toJson(fssFeeFetchRequest)
    }

    private fun prepareACKRequest(
        purchaseOrderNo: String,
        authRespCode: String,
        autID: String
    ): String {

        val feeObject = JSONObject(referenceId48)

        val txnStatus = if (authRespCode == "00") {
            "03"
        } else {
            "01"
        }
        val acknoledgemenrRequest = AcknoledgemenrRequest(
            totalAmount,
            "0007",
            feeObject.getString("correlationId"),
            "MPAY",
            purchaseOrderNo,
            ContextUtils.postDate(),
            authRespCode,
            autID,
            txnStatus,
            feeObject.getString("refId"),
            ContextUtils.acqTransactionCompletionDate(),
            purchaseOrderNo,
            "CC"
        )

        return Gson().toJson(acknoledgemenrRequest)
    }


    private fun prepareFeeReq(): PurchaseRequest {

        val decodedString2: ByteArray =
            Base64.decode(base64, Base64.DEFAULT)
        val text = String(decodedString2, StandardCharsets.UTF_8)
        val purchaseFeeRequest = PurchaseRequest(text)
        purchaseFeeRequest.MTI = "0100"
        purchaseFeeRequest.processingCode = "480000"//purchaseRequest.processingCode
        purchaseFeeRequest.amount = purchaseAmount
        purchaseFeeRequest.transDateAndTime = refId7

        purchaseFeeRequest.systemTraceAuditNumber = refId11
        purchaseFeeRequest.time = refId12
        purchaseFeeRequest.date = refId13

        val decodedString: ByteArray =
            Base64.decode(expiryDate, Base64.DEFAULT)
        val expiryTXT = String(decodedString, StandardCharsets.UTF_8)
        purchaseFeeRequest.expiryDate = expiryTXT

        purchaseFeeRequest.merchantType = sharedPreferenceUtil.getMerchantType()
        purchaseFeeRequest.posEntryMode = posEntryMode
        purchaseFeeRequest.posConditionCode = "51"//purchaseRequest.posConditionCode
        purchaseFeeRequest.cardAcceptorTerminalID = sharedPreferenceUtil.getTerminalID()

        purchaseFeeRequest.cardAcceptorIDCode = sharedPreferenceUtil.getMerchantID() + "      "
        purchaseFeeRequest.cardAcceptorNameLocation =
            sharedPreferenceUtil.getCardAcceptorNameLocation()

        purchaseFeeRequest.currencyCode = sharedPreferenceUtil.getCurrencyCode()
        return purchaseFeeRequest
    }

    private fun preparePartialFeeReq(purchaseRequest: PurchaseRequest): PurchaseRequest {

        val purchaseFeeRequest = PurchaseRequest(purchaseRequest.cardNumber)
        purchaseFeeRequest.MTI = "0100"
        purchaseFeeRequest.processingCode = "480000"//purchaseRequest.processingCode
        purchaseFeeRequest.amount = purchaseRequest.amount
        purchaseFeeRequest.transDateAndTime = purchaseRequest.transDateAndTime

        purchaseFeeRequest.systemTraceAuditNumber = purchaseRequest.systemTraceAuditNumber
        purchaseFeeRequest.time = purchaseRequest.time
        purchaseFeeRequest.date = purchaseRequest.date
        purchaseFeeRequest.expiryDate = purchaseRequest.expiryDate

        purchaseFeeRequest.merchantType = purchaseRequest.merchantType
        purchaseFeeRequest.posEntryMode = purchaseRequest.posEntryMode
        purchaseFeeRequest.posConditionCode = "51"
        purchaseFeeRequest.cardAcceptorTerminalID = purchaseRequest.cardAcceptorTerminalID

        purchaseFeeRequest.cardAcceptorIDCode = purchaseRequest.cardAcceptorIDCode
        purchaseFeeRequest.cardAcceptorNameLocation = purchaseRequest.cardAcceptorNameLocation

        purchaseFeeRequest.currencyCode = purchaseRequest.currencyCode
        return purchaseFeeRequest
    }

    private fun cancelTimer() {
        if (timer != null) {
            timer.cancel()
        }
    }
}
