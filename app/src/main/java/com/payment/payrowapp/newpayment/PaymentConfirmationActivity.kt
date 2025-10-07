package com.payment.payrowapp.newpayment

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityPaymentDoneBinding
import com.payment.payrowapp.databinding.ActivityPaymentSuccessfulBinding
import com.payment.payrowapp.dataclass.AcknoledgemenrRequest
import com.payment.payrowapp.dataclass.OrderRequest
import com.payment.payrowapp.dataclass.PurchaseRequest
import com.payment.payrowapp.dialogs.ReversalDialog
import com.payment.payrowapp.login.AuthenticationRepository
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.refundandreversal.RefundViewModel
import com.payment.payrowapp.refundandreversal.RefundViewModelFactory
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class PaymentConfirmationActivity : AppCompatActivity() {
    private var responseMessage: Int? = null
    var purchaseRequest: PurchaseRequest? = null
    var orderRequest: OrderRequest? = null
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    var paymentType: String? = null
    var bankTransferURL: String? = null
    var cardNumber: String? = null
    var ring: MediaPlayer? = null

    private lateinit var binding: ActivityPaymentDoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_payment_done)
        binding = ActivityPaymentDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        sharedPreferenceUtil = SharedPreferenceUtil(this)
        val bundle = intent.extras
        var cardNo = bundle?.getString("CARDNO")
        var invoiceNo = bundle?.getString("INVOICENO")
        val status = bundle?.getString("status")
        val sendBundle = Bundle()
        // sendBundle.putString("CARDNO", cardNo)
        sendBundle.putString("INVOICENO", invoiceNo)
        sendBundle.putString("hostRefNO", bundle?.getString("hostRefNO"))
        // sendBundle.putString("vasRefNO", bundle?.getString("vasRefNO"))
        sendBundle.putString("status", status)

        if (bundle?.getString("CardBrand") != null) {
            sendBundle.putString("CardBrand", bundle.getString("CardBrand"))
        }

        if (bundle?.getString("payRowDigitFee") != null) {
            sendBundle.putString("payRowDigitFee", bundle.getString("payRowDigitFee"))
        }

        // sendBundle.putString("amount",bundle?.getString("totalAmount"))

        //  payRowDigitFee = bundle.getString("PayRowDigFee");
        sendBundle.putFloat("payRowVATAmount", bundle!!.getFloat("payRowVATAmount"))
        sendBundle.putBoolean("payRowVATStatus", bundle.getBoolean("payRowVATStatus"))

        sendBundle.putBoolean("SignatureStatus", bundle.getBoolean("SignatureStatus"))
        sendBundle.putBoolean("isPinBlock", bundle.getBoolean("isPinBlock"))

        if (bundle.getString("authCode") != null) {
            sendBundle.putString("authCode", bundle.getString("authCode"))
        }

        if (bundle.getString("panSequenceNo") != null) {
            sendBundle.putString("panSequenceNo", bundle.getString("panSequenceNo"))
        }

        if (!bundle?.getString("paymentType").isNullOrEmpty()) {
            //  orderRequest = bundle!!.get("orderRequest") as OrderRequest
            paymentType = bundle.getString("paymentType")
            bankTransferURL = bundle.getString("bankTransferURL")

            //  orderRequest.amount = null
            //  orderRequest.payRowDigitialFee = null

            /*  if (orderRequest.referenceId48 != null) {
                  val feeResultObject = JSONObject(orderRequest.referenceId48)
                  sendBundle.putString(
                      "totalAmount",
                      feeResultObject.getString("totalChargableAmount")
                  )
                  sendBundle.putString("surcharges", feeResultObject.getString("surchargeAmount"))
              } else {
                  sendBundle.putString("totalAmount", bundle?.getString("totalAmount"))
              }*/

            sendBundle.putString("totalAmount", bundle?.getString("totalAmount"))
            if (status.equals("Cancelled")) {
                orderRequest = bundle.get("orderRequest") as OrderRequest
                orderRequest?.responseCode?.let {
                    ContextUtils.responseMessage(it)
                        ?.let { it1 ->
                            responseMessage = it1
                            sendBundle.putInt("responseMessage", it1)
                        }
                }
                //  if (orderRequest.errorTracking == null || !orderRequest.errorTracking!!.contains("Purchase")) {
                val enterPinActivityViewModel =
                    ViewModelProvider(
                        this,
                        EnterPinViewModelFactory(this)
                    )[EnterPinActivityViewModel::class.java]
                enterPinActivityViewModel.addOrder(
                    this,
                    orderRequest!!,
                    paymentType,
                    bankTransferURL
                )
                //    }
            } else if (status.equals("CAPTURED") || status.equals("NOT CAPTURED")) {

                val responseCode = bundle.getString("responseCode")
                responseCode?.let {
                    ContextUtils.responseMessage(it)
                        ?.let { it1 ->
                            responseMessage = it1
                            sendBundle.putInt("responseMessage", it1)
                        }
                }
            }
        }


        when (status) {
            /*"PARTIAL APPROVED" -> {
                tvConfirmation.text = getString(R.string.partially_approved)
                tvPaymentSuccessful.text = getString(R.string.response_ten)
                tvYouHaveMadePayment.text = getString(R.string.customer_payment_psrtilly_spproved)
            }*/
            "FAILED" -> {
                binding.tvConfirmation.text = getString(R.string.decline)
                //tvYouHaveMadePayment.visibility = View.GONE
                binding.tvYouHaveMadePayment.text = getString(R.string.customer_payment_declined)

                if (responseMessage != null) {
                    binding.tvPaymentSuccessful.text = getString(responseMessage!!)
                } else {
                    binding.tvPaymentSuccessful.text = getString(R.string.payment_Decline)
                }
                binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
            }

            "REVERSAL" -> {
                binding.tvConfirmation.text = getString(R.string.reversal)
                //  tvYouHaveMadePayment.visibility = View.GONE
                binding.tvPaymentSuccessful.text = getString(R.string.transaction_reversal)
                binding.tvYouHaveMadePayment.text = getString(R.string.customer_payement_reversal)

                binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
                binding.btnPaymentDetails.visibility = View.GONE
                ReversalDialog(this).show()
            }
            "NOT CAPTURED" -> {
                binding.tvConfirmation.text = getString(R.string.decline)
                //tvYouHaveMadePayment.visibility = View.GONE
                binding.tvYouHaveMadePayment.text = getString(R.string.customer_payment_declined)

                if (responseMessage != null) {
                    binding.tvPaymentSuccessful.text = getString(responseMessage!!)
                } else {
                    binding.tvPaymentSuccessful.text = getString(R.string.payment_Decline)
                }
                binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
            }
            "Cancelled" -> {
                binding.tvConfirmation.text = getString(R.string.decline)
                binding.tvYouHaveMadePayment.text = getString(R.string.customer_transaction_declined)

                val mCardType = sharedPreferenceUtil.getTransactionType()

                if (orderRequest?.responseCode.equals("-4122")) {
                    binding.tvPaymentSuccessful.text = getString(R.string.card_not_supported)
                } else if ((mCardType == 4 && orderRequest?.responseCode.equals("-4001")) ||
                    (mCardType == 4 && orderRequest?.responseCode.equals("-4141"))
                ) { //sharedPreferenceUtil.getAmount().toFloat() < 1000000) {
                    binding.tvPaymentSuccessful.text =
                        getString(R.string.please_perform_contact_transaction)
                } else if (orderRequest?.responseCode.equals("-4106") || orderRequest?.responseCode.equals(
                        "-4107"
                    )
                ) {
                    binding.tvPaymentSuccessful.text = getString(R.string.insert_swipe_tryanothercard)
                } else {
                    binding.tvPaymentSuccessful.text = getString(R.string.card_declined)
                }

                binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
            }
            "Terminal Declined" -> {

                purchaseRequest = PurchaseRequest(cardNo)
                constructPurchaseRequest(purchaseRequest!!)

                PaymentConfirmationRepository.reversalRequestAPI(
                    orderRequest!!, paymentType, bankTransferURL,
                    this@PaymentConfirmationActivity,
                    purchaseRequest!!
                )

                PaymentConfirmationRepository.getPurchaseResponse().observeOnce(this) {
                    if (it != null && it.purchaseResult.responseCode == "00") {

                        val feeResultObject = JSONObject(orderRequest?.referenceId48)
                        val purchaseFeeRequest = preparePartialFeeReq(purchaseRequest!!)
                        val ackPurchaseRequest = prepareACKRequest(
                            feeResultObject.getString("totalChargableAmount"),
                            it.purchaseResult.orderNo,
                            it.purchaseResult.responseCode,
                            it.purchaseResult.authorizationId
                        )

                        purchaseFeeRequest.fssFetch = ackPurchaseRequest
                        purchaseFeeRequest.posConditionCode = "52"
                        purchaseFeeRequest.processingCode = "490000"
                        // purchaseFeeRequest?.currencyCode = "784"
                        purchaseFeeRequest.ackCardBrand = orderRequest?.cardType
                        AuthenticationRepository.getFSSFeeAPI(
                            this,
                            purchaseFeeRequest, false
                        )

                        ReversalDialog(this).show()
                    }
                }
                /* AuthenticationRepository.sendPurchaseRequestAPI(
                     true, "", "", "", invoiceNo,
                     this@PaymentConfirmationActivity,
                     purchaseRequest!!
                 )*/
                binding.tvConfirmation.text = getString(R.string.decline)
                //tvYouHaveMadePayment.visibility = View.GONE
                binding.tvYouHaveMadePayment.text = getString(R.string.customer_transaction_declined)
                binding.tvPaymentSuccessful.text = getString(R.string.payment_Decline)
                binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
            }
        }

        binding.btnPaymentDetails.setOnClickListener {
          ring?.start()
            if (cardNo != null) {
                sendBundle.putString("CARDNO", cardNo)
            } else {
                sendBundle.putString("CARDNO", cardNumber)
            }

            cardNo = ""
            invoiceNo = ""
            startActivity(
                Intent(
                    this@PaymentConfirmationActivity,
                    CardReceiptActivity::class.java
                ).putExtras(sendBundle)
            )
            finish()
        }

        binding.btnPay.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }
    }

    private fun constructPurchaseRequest(purchaseRequest: PurchaseRequest): PurchaseRequest {

        purchaseRequest.reasonCode = "4000"

        if (orderRequest?.responseCode.equals("0") || orderRequest?.responseCode.equals("00")) {
            purchaseRequest.responseCode = "00"
        } else {
            purchaseRequest.responseCode = orderRequest?.responseCode
        }

        // purchaseRequest.track2Data = track2Data
        purchaseRequest.MTI = sharedPreferenceUtil.getVoidMTI()
        purchaseRequest.field90 =
            sharedPreferenceUtil.getMIT() + orderRequest?.referenceId11 + orderRequest?.referenceId7 + "0000000000" + "000000000000"//refId32+refId33
        purchaseRequest.processingCode = sharedPreferenceUtil.getProcessingCode()
        purchaseRequest.authorizationId = orderRequest?.authorizationId
        purchaseRequest.orderNo = orderRequest?.referenceId37


        purchaseRequest.switchKey =
            sharedPreferenceUtil.getCardAcceptorTerminalId() + ";" + orderRequest?.referenceId11 + ";" +
                    ContextUtils.getCurrentDate() + ContextUtils.getCurrentTime() + ";420"
        purchaseRequest.originalKey =
            sharedPreferenceUtil.getCardAcceptorTerminalId() + ";" + orderRequest?.referenceId11 + ";" + orderRequest?.referenceId7 +
                    ";200"

        purchaseRequest.time = orderRequest?.referenceId12
        purchaseRequest.date = orderRequest?.referenceId13
        purchaseRequest.transDateAndTime = orderRequest?.referenceId7

        if (orderRequest?.TRANSACTION_TYPE == 4 || orderRequest?.TRANSACTION_TYPE == 2) {
            purchaseRequest.appPANSeqNo = orderRequest?.cardsequencenumber
            purchaseRequest.fiftyFiveData = orderRequest?.ICCData
        }


        val decodedString: ByteArray =
            Base64.decode(orderRequest?.cardExpiryDate, Base64.DEFAULT)
        val expiryTXT = String(decodedString, StandardCharsets.UTF_8)
        purchaseRequest.expiryDate = expiryTXT


        purchaseRequest.amount =
            orderRequest?.purchaseAmount

        purchaseRequest.systemTraceAuditNumber = orderRequest?.referenceId11

        purchaseRequest.merchantType =
            sharedPreferenceUtil.getMerchantType()//Constants.MERCHANT_TYPE
        purchaseRequest.posEntryMode = orderRequest?.posEntryMode

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


    private fun prepareACKRequest(
        totalAmount: String,
        purchaseOrderNo: String,
        authRespCode: String,
        autID: String
    ): String {

        val feeObject = JSONObject(orderRequest?.referenceId48!!)

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
            orderRequest?.pmtTxnRefCode,
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

    private fun constructAutoReversal(purchaseRequest: PurchaseRequest): PurchaseRequest {

        purchaseRequest.reasonCode = "4021"

        //purchaseRequest.responseCode = "00"

        // purchaseRequest.track2Data = track2Data
        purchaseRequest.MTI = sharedPreferenceUtil.getVoidMTI()
        purchaseRequest.field90 =
            sharedPreferenceUtil.getMIT() + orderRequest?.referenceId11 + orderRequest?.referenceId7 + "0000000000" + "000000000000"//refId32+refId33
        purchaseRequest.processingCode = sharedPreferenceUtil.getProcessingCode()
        purchaseRequest.orderNo = orderRequest?.referenceId37


        purchaseRequest.switchKey =
            sharedPreferenceUtil.getCardAcceptorTerminalId() + ";" + orderRequest?.referenceId11 + ";" +
                    ContextUtils.getCurrentDate() + ContextUtils.getCurrentTime() + ";420"
        purchaseRequest.originalKey =
            sharedPreferenceUtil.getCardAcceptorTerminalId() + ";" + orderRequest?.referenceId11 + ";" + orderRequest?.referenceId7 +
                    ";200"

        purchaseRequest.time = orderRequest?.referenceId12
        purchaseRequest.date = orderRequest?.referenceId13
        purchaseRequest.transDateAndTime = orderRequest?.referenceId7

        if (orderRequest?.TRANSACTION_TYPE == 4 || orderRequest?.TRANSACTION_TYPE == 2) {
            purchaseRequest.appPANSeqNo = orderRequest?.cardsequencenumber
            purchaseRequest.fiftyFiveData = orderRequest?.ICCData
        }


        val decodedString: ByteArray =
            Base64.decode(orderRequest?.cardExpiryDate, Base64.DEFAULT)
        val expiryTXT = String(decodedString, StandardCharsets.UTF_8)
        purchaseRequest.expiryDate = expiryTXT


        purchaseRequest.amount =
            orderRequest?.purchaseAmount

        purchaseRequest.systemTraceAuditNumber = orderRequest?.referenceId11

        purchaseRequest.merchantType =
            sharedPreferenceUtil.getMerchantType()//Constants.MERCHANT_TYPE
        purchaseRequest.posEntryMode = orderRequest?.posEntryMode

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

    private fun getPaymentStatus(orderNumber: String?) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val refundViewModel =
            ViewModelProvider(
                this,
                RefundViewModelFactory(this)
            ).get(RefundViewModel::class.java)
        refundViewModel.getQrCodeInvoice(orderNumber!!)
        refundViewModel.getQrCodeInvoiceLiveData()
            .observeOnce(this@PaymentConfirmationActivity) {
                progressDialog.dismiss()
                if (it.data.get(0).purchaseBreakdown.service.size > 0) {
                    cardNumber = it.data.get(0).cardNumber
                }
                refundViewModel.reSetPurchaseResp()
            }
    }
}