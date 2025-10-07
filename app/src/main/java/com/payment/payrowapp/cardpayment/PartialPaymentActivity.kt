package com.payment.payrowapp.cardpayment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityEcommVoidrfactivityBinding
import com.payment.payrowapp.databinding.ActivityPartialPaymentBinding
import com.payment.payrowapp.dataclass.OrderRequest
import com.payment.payrowapp.dataclass.PurchaseRequest
import com.payment.payrowapp.dialogs.PartialDialog
import com.payment.payrowapp.newpayment.EnterPinActivityViewModel
import com.payment.payrowapp.newpayment.EnterPinViewModelFactory
import com.payment.payrowapp.newpayment.PaymentConfirmationActivity
import com.payment.payrowapp.otp.EnterOTPActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import java.io.Serializable
import java.nio.charset.StandardCharsets

class PartialPaymentActivity : AppCompatActivity() {

    lateinit var orderRequest: OrderRequest

    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var responseMessage: Int? = null
    var purchaseRequest: PurchaseRequest? = null
    var totalAmount: String? = null
    private lateinit var binding: ActivityPartialPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_partial_payment)
        binding = ActivityPartialPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferenceUtil = SharedPreferenceUtil(this)
        val bundle = intent.extras
        var cardNo = bundle?.getString("CARDNO")
        var invoiceNo = bundle?.getString("INVOICENO")
        val status = bundle?.getString("status")
        val sendBundle = Bundle()
        sendBundle.putString("CARDNO", cardNo)
        sendBundle.putString("INVOICENO", invoiceNo)
        sendBundle.putString("hostRefNO", bundle?.getString("hostRefNO"))
        sendBundle.putString("vasRefNO", bundle?.getString("vasRefNO"))
        sendBundle.putString("status", status)

        if (bundle?.getBoolean("SignatureStatus") != null) {
            sendBundle.putBoolean("SignatureStatus", bundle.getBoolean("SignatureStatus"))
        }

        if (bundle?.getBoolean("isPinBlock") != null) {
            sendBundle.putBoolean("isPinBlock", bundle.getBoolean("isPinBlock"))
        }

        if (bundle?.getString("authCode") != null) {
            sendBundle.putString("authCode", bundle.getString("authCode"))
        }

        if (bundle?.getString("panSequenceNo") != null) {
            sendBundle.putString("panSequenceNo", bundle.getString("panSequenceNo"))
        }

        orderRequest = bundle!!.get("orderRequest") as OrderRequest
        val paymentType = bundle.getString("paymentType")
        val bankTransferURL = bundle.getString("bankTransferURL")

        totalAmount = orderRequest.amount.toString()
        sendBundle.putString("totalAmount", totalAmount)

        binding.etEnterAmount.setText(orderRequest.PartialApprovedAmount.toString())

        val dueAmount = orderRequest.amount!! - orderRequest.PartialApprovedAmount!!
        binding.etCashReceived.setText(dueAmount.toString())

        orderRequest.responseCode?.let {
            ContextUtils.responseMessage(it)
                ?.let { it1 ->
                    responseMessage = it1
                    sendBundle.putInt("responseMessage", it1)
                }
        }

        orderRequest.amount = null
        orderRequest.payRowDigitialFee = null
        val enterPinActivityViewModel =
            ViewModelProvider(
                this,
                EnterPinViewModelFactory(this)
            )[EnterPinActivityViewModel::class.java]
        enterPinActivityViewModel.addOrder(
            this,
            orderRequest,
            paymentType,
            bankTransferURL
        )


        binding.btnCancelOrder.setOnClickListener {

            val intent = Intent(this, EnterOTPActivity::class.java)
            intent.putExtra("PurchaseRequest", purchaseRequest as Serializable)
            intent.putExtra("mode", "Void")
            intent.putExtra("hostRefNO", bundle.getString("hostRefNO"))
            intent.putExtra("vasRefNO", bundle.getString("vasRefNO"))
            intent.putExtra("cardNumber", cardNo)
            intent.putExtra("OrderNumber", invoiceNo)
            intent.putExtra("channel", "Partial")
            intent.putExtra("cardType", orderRequest.cardType)
            intent.putExtra("totalAmount", totalAmount)
            intent.putExtra("referenceId48", orderRequest.referenceId48)
            PartialDialog(this, intent).show()

            //  startActivity(intent)
            //  finish()
        }

        binding.btnContinue.setOnClickListener {
            cardNo = ""
            invoiceNo = ""
            startActivity(
                Intent(
                    this,
                    PaymentConfirmationActivity::class.java
                ).putExtras(sendBundle)
            )
            finish()
        }

        purchaseRequest = PurchaseRequest(cardNo)
        constructPurchaseRequest(purchaseRequest!!)
    }

    private fun constructPurchaseRequest(purchaseRequest: PurchaseRequest): PurchaseRequest {

        purchaseRequest.reasonCode = "4000"
        purchaseRequest.responseCode = "10"
        purchaseRequest.MTI = sharedPreferenceUtil.getVoidMTI()
        purchaseRequest.field90 =
            sharedPreferenceUtil.getMIT() + orderRequest.referenceId11 + orderRequest.referenceId7 + "0000000000" + "000000000000"//refId32+refId33
        purchaseRequest.processingCode = sharedPreferenceUtil.getProcessingCode()
        purchaseRequest.authorizationId = orderRequest.authorizationId
        purchaseRequest.orderNo = orderRequest.referenceId37


        purchaseRequest.switchKey =
            sharedPreferenceUtil.getCardAcceptorTerminalId() + ";" + orderRequest.referenceId11 + ";" +
                    ContextUtils.getCurrentDate() + ContextUtils.getCurrentTime() + ";420"
        purchaseRequest.originalKey =
            sharedPreferenceUtil.getCardAcceptorTerminalId() + ";" + orderRequest.referenceId11 + ";" + orderRequest.referenceId7 +
                    ";200"

        purchaseRequest.time = orderRequest.referenceId12
        purchaseRequest.date = orderRequest.referenceId13
        purchaseRequest.transDateAndTime = orderRequest.referenceId7

        if (orderRequest.TRANSACTION_TYPE == 4 || orderRequest.TRANSACTION_TYPE == 2) {
            purchaseRequest.appPANSeqNo = orderRequest.cardsequencenumber
            purchaseRequest.fiftyFiveData = orderRequest.ICCData
        }


        val decodedString: ByteArray =
            Base64.decode(orderRequest.cardExpiryDate, Base64.DEFAULT)
        val expiryTXT = String(decodedString, StandardCharsets.UTF_8)
        purchaseRequest.expiryDate = expiryTXT


        purchaseRequest.amount =
            orderRequest.purchaseAmount

        purchaseRequest.systemTraceAuditNumber = orderRequest.referenceId11

        purchaseRequest.merchantType =
            sharedPreferenceUtil.getMerchantType()//Constants.MERCHANT_TYPE
        purchaseRequest.posEntryMode = orderRequest.posEntryMode

        purchaseRequest.posConditionCode =
            sharedPreferenceUtil.getPOSConditionCode()//Constants.POS_CONDITION_CODE
        // purchaseRequest.acquirerInstIdCode = refId32!!//sharedPreferenceUtil.getAccInstIdCode()
        purchaseRequest.cardAcceptorTerminalID = sharedPreferenceUtil.getTerminalID()
        purchaseRequest.cardAcceptorIDCode = sharedPreferenceUtil.getMerchantID() + "      "
        purchaseRequest.cardAcceptorNameLocation =
            sharedPreferenceUtil.getCardAcceptorNameLocation()
        purchaseRequest.currencyCode =
            sharedPreferenceUtil.getCurrencyCode()//Constants.CURRENCY_CODE
        purchaseRequest.sponsorBank = sharedPreferenceUtil.getSponsorBank()//Constants.SPONSOR_BANK
        return purchaseRequest
    }
}