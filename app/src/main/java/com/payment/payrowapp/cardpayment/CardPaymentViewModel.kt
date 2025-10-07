package com.payment.payrowapp.cardpayment

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.mastercloud.CPOCConnectActivity
import com.payment.payrowapp.newpayment.CardReceiptActivityRepository
import com.payment.payrowapp.newpayment.PaymentConfirmationActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.sunmipay.ICProcessActivity
import com.payment.payrowapp.sunmipay.TransactionCallback
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.MoneyUtil
import com.payment.payrowapp.wizzit.PaymentActivityRepository
import org.json.JSONObject
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class CardPaymentViewModel : ViewModel() {
    private var posId: String? = null
    private var mainMerchantId: String? = null
    private var customerEmail: String? = null
    private var customerPhone: String? = null
    private var posType: String? = null
    private var payrowInvoiceNo: String? = null
    private var trnNo: String? = null
    private var receiptNo: String? = null
    private var merchantEmail: String? = null
    private var userId: String? = null
    private var distributorId: String? = null
    private var storeId: String? = null
    private var bankTransferURL: String? = null
    private var customerBillingPostalCode: String? = null
    private var customerBillingState: String? = null
    private var customerBillingCity: String? = null
    private var customerBillingCountry: String? = null
    private var customerName: String? = null
    private var vasRefNO: Int? = null
    private var hostRefNO: Int? = null
    private var currentDate: String? = null
    private var encryptText: String? = null
    private lateinit var channel: String
    private var checkoutID: String? = null
    private var orderNumber: String? = null

    private var aid: String? = null
    private var TVR: String? = null
    private var AC_INFO: String? = null
    private var AC: String? = null
    private var TRANSACTION_TYPE: Int? = null
    private var CardType: String? = null
    private var cardBrand: String? = null
    private var pmtTxnRefCode: String? = null


    private fun getCurrentDate(): String {
        val calender = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("MMdd", Locale.getDefault())
        return dateFormat.format(calender)
    }

    private fun getCurrentTime(): String {
        val calender = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
        return dateFormat.format(calender)
    }


    fun randomValue(): Int {
        return (10000..99999).shuffled().last()
    }

    private fun getHexaDecimal(amount: String): String {
        var myAmount = amount
        when (amount.length) {
            1 -> {
                myAmount = "000000000" + amount + "00"
            }
            2 -> {
                myAmount = "00000000" + amount + "00"
            }
            3 -> {
                myAmount = "000000000$amount"
            }
            4 -> {
                myAmount = "00000000$amount"
            }
            5 -> {
                myAmount = "0000000$amount"
            }
            6 -> {
                myAmount = "000000$amount"
            }
            7 -> {
                myAmount = "00000$amount"
            }
            8 -> {
                myAmount = "0000$amount"
            }
            else -> {
                myAmount = "000000000000"
            }
        }

        return myAmount
    }

    fun constructPurchaseRequest(
        isPinBlock: Boolean,
        purchaseRequest: PurchaseRequest,
        sharedPreferenceUtil: SharedPreferenceUtil, amount: String
    ): PurchaseRequest {

        var amountValue = amount.toFloat()
        amountValue *= 100
        val fAmount = (amountValue + 0.1).toInt()

        purchaseRequest.terminalOwner = "Default Participant"
        purchaseRequest.amount = getHexaDecimal(fAmount.toString())
        purchaseRequest.MTI = sharedPreferenceUtil.getMIT()//Constants.MTI
        purchaseRequest.processingCode = sharedPreferenceUtil.getProcessingCode()
        //  purchaseRequest.amount = ContextUtils.getStringInHexaDecimal(amount)
        purchaseRequest.transDateAndTime =
            getCurrentDate() + getCurrentTime()
        purchaseRequest.systemTraceAuditNumber =
            sharedPreferenceUtil.getSystemTraceAuditNumber()
        purchaseRequest.time = getCurrentTime()
        purchaseRequest.date = getCurrentDate()
        purchaseRequest.merchantType =
            sharedPreferenceUtil.getMerchantType()//Constants.MERCHANT_TYPE

        val mCardType = sharedPreferenceUtil.getTransactionType()

        // nfc
        if (mCardType == 4 && amount.toFloat() <= 500) {
            purchaseRequest.posEntryMode = "071"

            if (isPinBlock) {
                purchaseRequest.posPINCaptureCode = "12"
            }
        } else if (mCardType == 4 && amount.toFloat() > 500) {
            purchaseRequest.posPINCaptureCode = "12"
            purchaseRequest.posEntryMode = "071"
        } else {
            purchaseRequest.posPINCaptureCode = "12"
        }


        //ic
        if (mCardType == 2) {
            purchaseRequest.posEntryMode = "051"
        }

        // magnetic, fallback
        if (mCardType == 1) {
            purchaseRequest.posEntryMode = "901"
        } /*else {
                purchaseRequest.appPANSeqNo = sharedPreferenceUtil.getAppPanSequenceNo()
            }*/


        purchaseRequest.posConditionCode =
            sharedPreferenceUtil.getPOSConditionCode()//Constants.POS_CONDITION_CODE

        purchaseRequest.cardAcceptorTerminalID =
            sharedPreferenceUtil.getTerminalID()
        purchaseRequest.cardAcceptorIDCode =
            sharedPreferenceUtil.getMerchantID() + "      "//"782334000      "
        purchaseRequest.cardAcceptorNameLocation =
            sharedPreferenceUtil.getCardAcceptorNameLocation()
        purchaseRequest.currencyCode =
            sharedPreferenceUtil.getCurrencyCode()//Constants.CURRENCY_CODE
        // purchaseRequest.posGeoData = sharedPreferenceUtil.getPosGeoData()//Constants.POS_GEO_DATA
        purchaseRequest.sponsorBank =
            sharedPreferenceUtil.getSponsorBank()//Constants.SPONSOR_BANK

        return purchaseRequest
    }


    fun initiateTransaction(
        orderedNumber: String,
        feeResponseData: FeeResponseData,
        payRowDigitFee: String,
        payRowVATStatus: Boolean, payRowVATAmount: Float,
        pinAvaible: Boolean,
        isPinBlock: Boolean,
        onlineStatus: Boolean,
        transactionCallback: TransactionCallback, signatureStatus: Boolean,
        mpinType: Int,
        cardNumber: String,
        totalAmount: String,
        expirydate: String,
        paymentTypeChan: String,
        amount: String,
        purchaseRequest: PurchaseRequest,
        icProcessActivity: ICProcessActivity,
        sharedPreferenceUtil: SharedPreferenceUtil
    ) {
        orderNumber = orderedNumber
        /* if (sharedPreferenceUtil.getOrderNum().isNotEmpty()) {
             orderNumber = sharedPreferenceUtil.getOrderNum()
             sharedPreferenceUtil.setOrderNum("")
         } */

        //prepare order
        val calendar = Calendar.getInstance().time
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        currentDate = sdf.format(calendar)

        //val receiptNO = (100..999).shuffled().last()

        vasRefNO = randomValue()


        if (sharedPreferenceUtil.getQRStoreID().isNotEmpty()) {
            storeId = sharedPreferenceUtil.getQRStoreID()
            sharedPreferenceUtil.setQRStoreID("")
        } else {
            storeId = sharedPreferenceUtil.getStoreID()
        }

        if (mainMerchantId.isNullOrEmpty()) {
            mainMerchantId = sharedPreferenceUtil.getMerchantID()
        }

        if (sharedPreferenceUtil.getQRReNo().isNotEmpty()) {
            receiptNo = sharedPreferenceUtil.getQRReNo()
            sharedPreferenceUtil.setQRReNo("")
        } else {
            receiptNo = (100..999).shuffled().last().toString()
        }

        if (sharedPreferenceUtil.getQRPosType().isNotEmpty()) {
            posType = sharedPreferenceUtil.getQRPosType()
            sharedPreferenceUtil.setQRPosType("")
        } else {
            posType = sharedPreferenceUtil.getUserRole()
        }

        if (sharedPreferenceUtil.getQRPosID().isNotEmpty()) {
            posId = sharedPreferenceUtil.getQRPosID()
            sharedPreferenceUtil.setQRPosID("")
        } else {
            posId = sharedPreferenceUtil.getUserID()
        }

        if (sharedPreferenceUtil.getQRDistID().isNotEmpty()) {
            distributorId = sharedPreferenceUtil.getQRDistID()
            sharedPreferenceUtil.setQRDistID("")
        } else {
            distributorId = sharedPreferenceUtil.getDistributorID()
        }

        if (sharedPreferenceUtil.getQRUser().isNotEmpty()) {
            userId = sharedPreferenceUtil.getQRUser()
            sharedPreferenceUtil.setQRUser("")
        } else {
            userId = sharedPreferenceUtil.getUserID()
        }

        if (sharedPreferenceUtil.getQRMerEmail().isNotEmpty()) {
            merchantEmail = sharedPreferenceUtil.getQRMerEmail()
            sharedPreferenceUtil.setQRMerEmail("")
        } else {
            merchantEmail = sharedPreferenceUtil.getEmailID()
        }

        if (sharedPreferenceUtil.getQRCPhone().isNotEmpty()) {
            customerPhone = sharedPreferenceUtil.getQRCPhone()
            sharedPreferenceUtil.setQRCPhone("")
        } else {
            customerPhone = sharedPreferenceUtil.getMerchantMobileNumber()
        }

        if (sharedPreferenceUtil.getQRCEmail().isNotEmpty()) {
            customerEmail = sharedPreferenceUtil.getQRCEmail()
            sharedPreferenceUtil.setQRCEmail("")
        } else {
            customerEmail = sharedPreferenceUtil.getEmailID()
        }

        if (sharedPreferenceUtil.getQRTrNo().isNotEmpty()) {
            trnNo = sharedPreferenceUtil.getQRTrNo()
            sharedPreferenceUtil.setQRTrNo("")
        } else {
            trnNo = randomValue().toString()
        }

        if (sharedPreferenceUtil.getQRPInvoiceNo().isNotEmpty()) {
            payrowInvoiceNo = sharedPreferenceUtil.getQRPInvoiceNo()
            sharedPreferenceUtil.setQRPInvoiceNo("")
        } else {
            payrowInvoiceNo = randomValue().toString()
        }

        if (sharedPreferenceUtil.getQRTransURL().isNotEmpty()) {
            bankTransferURL = sharedPreferenceUtil.getQRTransURL()
            sharedPreferenceUtil.setQRTransURL("")
        }

        if (sharedPreferenceUtil.getQRCheckID().isNotEmpty()) {
            checkoutID = sharedPreferenceUtil.getQRCheckID()
            sharedPreferenceUtil.setQRCheckID("")
        }

        if (sharedPreferenceUtil.getQRCName().isNotEmpty()) {
            customerName = sharedPreferenceUtil.getQRCName()
            sharedPreferenceUtil.setQRCName("")
        } else {
            customerName = sharedPreferenceUtil.getMerchantName()
        }

        if (sharedPreferenceUtil.getQRCCity().isNotEmpty()) {
            customerBillingCity = sharedPreferenceUtil.getQRCCity()
            sharedPreferenceUtil.setQRCCity("")
        } else {
            customerBillingCity = sharedPreferenceUtil.getCity()
        }

        if (sharedPreferenceUtil.getQRCState().isNotEmpty()) {
            customerBillingState = sharedPreferenceUtil.getQRCState()
            sharedPreferenceUtil.setQRCState("")
        } else {
            customerBillingState = sharedPreferenceUtil.getCity()
        }

        if (sharedPreferenceUtil.getQRCCountry().isNotEmpty()) {
            customerBillingCountry = sharedPreferenceUtil.getQRCCountry()
            sharedPreferenceUtil.setQRCCountry("")
        } else {
            customerBillingCountry = sharedPreferenceUtil.getCountry()
        }

        if (sharedPreferenceUtil.getQRCPCode().isNotEmpty()) {
            customerBillingPostalCode = sharedPreferenceUtil.getQRCPCode()
            sharedPreferenceUtil.setQRCPCode("")
        } else {
            customerBillingPostalCode = sharedPreferenceUtil.getBOBox()
        }

        channel = if (paymentTypeChan == "scan qr") {
            "Third Party QRCode"
        } else {
            Constants.CARD// preference?.getString(Constants.TRANSACTION_TYPE, "")!!
        }

        try {
            encryptText =
                cardNumber.substring(0, 6)
                    .padEnd(cardNumber.length - 4, '*') + cardNumber.substring(
                    12,
                    16
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val expiryDatedata =
            expirydate.toByteArray(StandardCharsets.UTF_8)
        val base64ExpiryDate = Base64.encodeToString(expiryDatedata, Base64.DEFAULT)


        TRANSACTION_TYPE = sharedPreferenceUtil.getTransactionType()
        if (sharedPreferenceUtil.getAID() != "" && TRANSACTION_TYPE != 1) {
            aid = sharedPreferenceUtil.getAID()

            val isVisa: Boolean = aid!!.startsWith("A000000003")
            val isMaster = (aid!!.startsWith("A000000004") || aid!!.startsWith("A000000005"))

            if (isVisa) {
                CardType = "Visa"
            } else if (isMaster) {
                if (TRANSACTION_TYPE == 4 && !isPinBlock) {//amount.toFloat() <= 500
                    purchaseRequest.structuredData = "212Postilion:TM21416ScaInd14TMVL"
                }

                CardType = "MasterCard"
            }

            if (sharedPreferenceUtil.getTVR() != "") {
                TVR = sharedPreferenceUtil.getTVR()
            }

            if (sharedPreferenceUtil.getACInfo() != "") {
                AC_INFO = sharedPreferenceUtil.getACInfo()
            }

            if (sharedPreferenceUtil.getAC() != "") {
                AC = sharedPreferenceUtil.getAC()
            }
        } else {
            aid = null
            CardType = null
            TVR = null
            AC_INFO = null
            AC = null
        }

        // create for negative case
        val orderRequest = orderRequest(
            payRowDigitFee,
            amount, payRowVATStatus, payRowVATAmount,
            signatureStatus, pinAvaible,
            sharedPreferenceUtil.getReportID(),
            totalAmount,
            base64ExpiryDate,
            sharedPreferenceUtil.getBussinessId(),
            cardNumber,
            purchaseRequest.amount!!,
            orderNumber!!,
            sharedPreferenceUtil.getTerminalID(),
            sharedPreferenceUtil.getMerchantEmail(),
            sharedPreferenceUtil.getMerchantPhone(),
            purchaseRequest.posEntryMode!!,
            purchaseRequest.track2Data!!,
            purchaseRequest.fiftyFiveData,
            purchaseRequest.appPANSeqNo
        )
        // pinBlockRequest?.pin = ""//txt_pin_entry.text.toString()

         cardBrand = if (TRANSACTION_TYPE != 1) {
            getCardBrand(aid)
        } else {
            ContextUtils.getCardBrand(cardNumber)
        }
        val purchaseFeeRequest =
            prepareFeeReq(
                expirydate,
                totalAmount,
                cardBrand,
                feeResponseData.servicedata,
                purchaseRequest
            )


        val progressDialog = ProgressDialog(icProcessActivity)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        PaymentActivityRepository.getFSSFeeAPI(
            true,
            icProcessActivity,
            purchaseFeeRequest, onlineStatus, transactionCallback,
            orderRequest, channel, bankTransferURL, pinAvaible, signatureStatus
        )

        PaymentActivityRepository.getFSSFeeResponse().observeOnce(icProcessActivity) {
            progressDialog.dismiss()
            if (it.purchaseResult.responseCode == "00") {
                purchaseRequest.orderNo = it.purchaseResult.orderNo
                purchaseFeeRequest.orderNo = it.purchaseResult.orderNo

                val feeResultObject = JSONObject(it.purchaseResult.fssFetch)

                var amountValue = feeResultObject.getString("totalChargableAmount").toFloat()
                amountValue *= 100
                val fAmount = (amountValue + 0.1).toInt()
                purchaseRequest.amount = getHexaDecimal(fAmount.toString())
                orderRequest.referenceId48 = it.purchaseResult.fssFetch
                orderRequest.referenceId12 = purchaseRequest.time
                orderRequest.referenceId13 = purchaseRequest.date
                orderRequest.referenceId7 = purchaseRequest.transDateAndTime
                orderRequest.referenceId37 = purchaseRequest.orderNo
                orderRequest.referenceId11 = purchaseRequest.systemTraceAuditNumber

                sendPurchaseReq(it.purchaseResult.fssFetch,
                    feeResultObject.getString("refId"),
                    purchaseFeeRequest,
                    onlineStatus,
                    transactionCallback,
                    cardNumber,
                    totalAmount, base64ExpiryDate, purchaseRequest, icProcessActivity,
                    sharedPreferenceUtil, orderRequest, pinAvaible, signatureStatus
                )
            } else {
                orderRequest.responseCode = it.purchaseResult.responseCode
                orderRequest.errorTracking = "Possibility"
                val failedRequstClass =
                    FailedRequstClass(
                        orderRequest,
                        channel,
                        bankTransferURL, onlineStatus, signatureStatus
                    )
                transactionCallback.onTransactionFailed(failedRequstClass, pinAvaible)
                /*val ackPurchaseRequest = prepareACKRequest(
                    null,
                    it.purchaseResult.responseCode,
                    it.purchaseResult.authorizationId,
                    purchaseFeeRequest
                )

                purchaseFeeRequest.fssFetch = ackPurchaseRequest
                purchaseFeeRequest.posConditionCode = "52"
                purchaseFeeRequest.processingCode = "490000"
                // purchaseFeeRequest?.currencyCode = "784"
                purchaseFeeRequest.ackCardBrand = CardType
                PaymentActivityRepository.getFSSFeeAPI(
                    false,
                    icProcessActivity,
                    purchaseFeeRequest, onlineStatus, transactionCallback,
                    orderRequest, channel, bankTransferURL, pinAvaible, signatureStatus
                )*/
            }
        }
    }

    fun sendPurchaseReq(referenceId48:String,
        feeRefId: String,
        purchaseFeeRequest: PurchaseRequest,
        onlineStatus: Boolean,
        transactionCallback: TransactionCallback,
        cardNumber: String,
        totalAmount: String,
        base64ExpiryDate: String,
        purchaseRequest: PurchaseRequest,
        icProcessActivity: ICProcessActivity,
        sharedPreferenceUtil: SharedPreferenceUtil,
        orderRequest: OrderRequest, pinAvaible: Boolean, signatureStatus: Boolean
    ) {

        val progressDialog = ProgressDialog(icProcessActivity)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        sendPurchaseRequest(
            onlineStatus, transactionCallback,
            icProcessActivity,
            purchaseRequest, orderRequest, channel, bankTransferURL, pinAvaible, signatureStatus
        )

        purchaseResponse().observeOnce(icProcessActivity) {

            if (it != null) {
                var partialAmount: String? = null
                var purchaseAmount: String? = null
                val status =
                    when (it.purchaseResult.responseCode) {
                        "00" -> {
                            "CAPTURED"
                        }
                        "10" -> {
                            "PARTIAL APPROVED"
                        }
                        else -> {
                            "NOT CAPTURED"
                        }
                    }


                if (status == "PARTIAL APPROVED") {
                    try {
                        purchaseAmount = MoneyUtil.decimalToEMVHex(it.purchaseResult.amount)
                        partialAmount =
                            MoneyUtil.parseAmountFromEMV("9F0206$purchaseAmount")
                        println("Parsed Amount: $$partialAmount")
                    } catch (e: Exception) {
                        System.err.println("Error parsing EMV data: " + e.message)
                    }
                } else {
                    purchaseAmount = purchaseRequest.amount
                }

                /* try {
                     purchaseAmount = MoneyUtil.decimalToEMVHex("2000")
                     partialAmount =
                         MoneyUtil.parseAmountFromEMV("9F0206$purchaseAmount")
                     println("Parsed Amount: $$partialAmount")
                 } catch (e: Exception) {
                     System.err.println("Error parsing EMV data: " + e.message)
                 }*/

                val data =
                    cardNumber.toByteArray(StandardCharsets.UTF_8)
                val base64 = Base64.encodeToString(data, Base64.DEFAULT)
                val respOrderRequest = OrderRequest(
                    storeId!!,
                    orderNumber!!,
                    Constants.CARD,
                    mainMerchantId!!,
                    posId!!,
                    posType!!,
                    customerEmail!!,
                    customerPhone!!,
                    distributorId!!,
                    null,
                    userId!!,
                    null,
                    null,
                    null,
                    sharedPreferenceUtil.getMerchantEmail(),
                    sharedPreferenceUtil.getMerchantPhone(),
                    trnNo,
                    receiptNo,
                    null,
                    payrowInvoiceNo,
                    null,
                    null,
                    encryptText,
                    it.purchaseResult.orderNo,
                    vasRefNO.toString(),
                    status,
                    customerName,
                    sharedPreferenceUtil.getMerchantEmail(),
                    sharedPreferenceUtil.getMerchantPhone(),
                    customerBillingCity,
                    customerBillingState,
                    customerBillingCountry,
                    customerBillingPostalCode,
                    checkoutID,
                    null,
                    it.purchaseResult.RefundNum,
                    it.purchaseResult.transDateAndTime,
                    it.purchaseResult.systemTraceAuditNumber,
                    it.purchaseResult.time,
                    purchaseRequest.date,
                    it.purchaseResult.orderNo,
                    it.purchaseResult.acquirerInstIdCode,
                    it.purchaseResult.refund33,
                    it.purchaseResult.responseCode,
                    "Purchase",
                    it.purchaseResult.authorizationId,
                    base64,
                    null,
                    purchaseAmount,
                    sharedPreferenceUtil.getTerminalID(),
                    customerEmail,
                    customerPhone,
                    sharedPreferenceUtil.getBussinessId(),
                    base64ExpiryDate,
                    it.purchaseResult.orderNo,
                    status,
                    totalAmount.toFloat(),
                    orderRequest.payRowDigitialFee,
                    cardBrand,
                    TVR,
                    AC_INFO,
                    AC,
                    aid,
                    TRANSACTION_TYPE,
                    purchaseRequest.track2Data,
                    purchaseRequest.posEntryMode,
                    purchaseRequest.fiftyFiveData,
                    purchaseRequest.appPANSeqNo,
                    sharedPreferenceUtil.getReportID(),
                    signatureStatus,
                    pinAvaible,
                    partialAmount?.toFloat(),
                    it.purchaseResult.referenceId127_3,
                    it.purchaseResult.referenceId127_22,
                    it.purchaseResult.iccData,
                    it.purchaseResult.extendedResponseCode,
                    it.purchaseResult.amount,
                    it.purchaseResult.cardAcceptorTerminalID,
                    it.purchaseResult.cardAcceptorIDCode,
                    it.purchaseResult.cardAcceptorNameLocation,
                    HeaderSignatureUtil.getDeviceSN(),
                    null,
                    orderRequest.vatStatus,
                    orderRequest.vatAmount,referenceId48,pmtTxnRefCode
                )

                progressDialog.dismiss()
                if (onlineStatus) {
                    val resultRequestClass = ResultRequestClass(
                        respOrderRequest,
                        orderNumber,
                        cardNumber,
                        it.purchaseResult.orderNo,
                        vasRefNO.toString(),
                        status,
                        channel,
                        bankTransferURL,
                        it.purchaseResult.authorizationId,
                        purchaseRequest.appPANSeqNo, it.purchaseResult.iccData, signatureStatus
                    )

                    reSetPurchaseResp()

                    if (it.purchaseResult.responseCode == "65" && it.purchaseResult.extendedResponseCode == "1600") {

                        val singleTapPinRequest =
                            SingleTapPinRequest(
                                feeRefId, purchaseFeeRequest,
                                respOrderRequest,
                                purchaseRequest,
                                base64ExpiryDate,
                                cardNumber,
                                totalAmount,referenceId48
                            )
                        transactionCallback.singleTapPinRequest(singleTapPinRequest)
                    } else {

                        val feeResultObject = JSONObject(referenceId48)
                        val ackPurchaseRequest = prepareACKRequest(feeResultObject.getString("totalChargableAmount"),
                            feeRefId,
                            it.purchaseResult.responseCode,
                            it.purchaseResult.authorizationId,
                            purchaseFeeRequest
                        )

                        purchaseFeeRequest.amount = purchaseAmount
                        purchaseFeeRequest.fssFetch = ackPurchaseRequest
                        purchaseFeeRequest.posConditionCode = "52"
                        purchaseFeeRequest.processingCode = "490000"
                        // purchaseFeeRequest?.currencyCode = "784"
                        purchaseFeeRequest.ackCardBrand = cardBrand
                        PaymentActivityRepository.getFSSFeeAPI(
                            false,
                            icProcessActivity,
                            purchaseFeeRequest, onlineStatus, transactionCallback,
                            orderRequest, channel, bankTransferURL, pinAvaible, signatureStatus
                        )
                        transactionCallback.onTransactionStatus(resultRequestClass, pinAvaible)
                    }
                } else {

                    val feeResultObject = JSONObject(referenceId48)
                    val ackPurchaseRequest = prepareACKRequest(feeResultObject.getString("totalChargableAmount"),
                        feeRefId,
                        it.purchaseResult.responseCode,
                        it.purchaseResult.authorizationId,
                        purchaseFeeRequest
                    )

                    purchaseFeeRequest.amount = purchaseAmount
                    purchaseFeeRequest.fssFetch = ackPurchaseRequest
                    purchaseFeeRequest.posConditionCode = "52"
                    purchaseFeeRequest.processingCode = "490000"
                    // purchaseFeeRequest?.currencyCode = "784"
                    purchaseFeeRequest.ackCardBrand = cardBrand
                    PaymentActivityRepository.getFSSFeeAPI(
                        false,
                        icProcessActivity,
                        purchaseFeeRequest, onlineStatus, transactionCallback,
                        orderRequest, channel, bankTransferURL, pinAvaible, signatureStatus
                    )
                    val bundle1 = Bundle()
                    bundle1.putString("TYPE", "TAPTOPAY")
                    bundle1.putString("CARDNO", cardNumber)
                    bundle1.putString("INVOICENO", orderNumber)
                    bundle1.putString("hostRefNO", it.purchaseResult.orderNo)
                    bundle1.putString("vasRefNO", vasRefNO.toString())
                    bundle1.putString("status", status)
                    bundle1.putString("paymentType", channel)
                    bundle1.putString("bankTransferURL", bankTransferURL)
                    bundle1.putString("authCode", it.purchaseResult.authorizationId)
                    bundle1.putString("panSequenceNo", purchaseRequest.appPANSeqNo)
                    bundle1.putBoolean("isPinBlock", pinAvaible)
                    bundle1.putBoolean("SignatureStatus", signatureStatus)
                    bundle1.putFloat("payRowVATAmount", orderRequest.vatAmount!!)
                    bundle1.putBoolean("payRowVATStatus", orderRequest.vatStatus!!)
                    bundle1.putString("totalAmount", totalAmount)
                    bundle1.putString("payRowDigitFee", orderRequest.payRowDigitialFee.toString())
                    // amount = ""

                    reSetPurchaseResp()
                    icProcessActivity.startActivity(
                        Intent(
                            icProcessActivity,
                            PaymentConfirmationActivity::class.java
                        ).putExtras(bundle1)
                            .putExtra("orderRequest", respOrderRequest as Serializable)
                    )
                    icProcessActivity.finish()
                }
            }
        }
    }

    private fun prepareACKRequest(txnAmount: String?,
        feeRefId: String?,
        authRespCode: String,
        autID: String?,
        purchaseFeeRequest: PurchaseRequest
    ): String {
        val fssFeeFetchReqest = Gson().fromJson(
            purchaseFeeRequest.fssFetch,
            FSSFeeFetchReqest::class.java
        )

        val txnStatus = if (authRespCode == "00" || authRespCode == "10") {
            "00"
        } else {
            "01"
        }
        val acknoledgemenrRequest = AcknoledgemenrRequest(
            txnAmount,
            fssFeeFetchReqest.tranType,
            fssFeeFetchReqest.correlationId,
            fssFeeFetchReqest.schemeId,
            fssFeeFetchReqest.pmtTxnRefCode,
            ContextUtils.postDate(),
            authRespCode,
            autID,
            txnStatus,
            feeRefId,
            ContextUtils.acqTransactionCompletionDate(),
            purchaseFeeRequest.orderNo,
            "CC"
        )

        return Gson().toJson(acknoledgemenrRequest)
    }

    private fun orderRequest(
        payRowDigitFee: String,
        amount: String, payRowVATStatus: Boolean, payRowVATAmount: Float,
        signatureStatus: Boolean, isPinBlock: Boolean,
        storeManagerID: String,
        totalAmountCharge: String,
        expiryDate: String,
        bussinessId: String,
        cardNumber: String,
        purchaseHexAmount: String,
        orderNo: String,
        terminalID: String,
        merchantMail: String,
        merchantPhone: String,
        posEntryMode: String,
        track2Data: String, fiftyFive: String?, panSequenceNum: String?
    ): OrderRequest {
        val data = cardNumber.toByteArray(StandardCharsets.UTF_8)
        val base64 = Base64.encodeToString(data, Base64.DEFAULT)
        return OrderRequest(
            storeId!!,
            orderNo,
            Constants.CARD,
            mainMerchantId!!,
            posId!!,
            posType!!,
            customerEmail!!,
            customerPhone!!,
            distributorId!!,
            null,
            userId!!,
            null,
            null,
            null,
            merchantMail,
            merchantPhone,
            trnNo,
            receiptNo,
            null,
            payrowInvoiceNo,
            null,
            null,
            encryptText,
            null,
            vasRefNO.toString(),
            "NOT CAPTURED",
            customerName,
            merchantMail,
            merchantPhone,
            customerBillingCity,
            customerBillingState,
            customerBillingCountry,
            customerBillingPostalCode,
            checkoutID,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "Purchase",
            null,
            base64,
            null,
            purchaseHexAmount,
            terminalID,
            customerEmail,
            customerPhone,
            bussinessId,
            expiryDate,
            null,
            "NOT CAPTURED",
            totalAmountCharge.toFloat(),
            payRowDigitFee.toFloat(),
            cardBrand,
            TVR,
            AC_INFO,
            AC,
            aid,
            TRANSACTION_TYPE,
            track2Data,
            posEntryMode,
            fiftyFive,
            panSequenceNum,
            storeManagerID,
            signatureStatus,
            isPinBlock,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, HeaderSignatureUtil.getDeviceSN(), null, payRowVATStatus, payRowVATAmount,null,null)
    }

    fun cardDeclineUpdate(
        orderedNumber: String,
        payRowDigitFee: String,
        amount: String, payRowVATStatus: Boolean, payRowVATAmount: Float,
        signatureStatus: Boolean, isPinBlock: Boolean,
        totalAmount: String,
        paymentTypeChan: String,
        icProcessActivity: CPOCConnectActivity, sharedPreferenceUtil: SharedPreferenceUtil,
        message: String, code: String
    ) {

       // TRANSACTION_TYPE = sharedPreferenceUtil.getTransactionType()

        orderNumber = orderedNumber

        //prepare order
        val calendar = Calendar.getInstance().time
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        currentDate = sdf.format(calendar)

        //val receiptNO = (100..999).shuffled().last()

        vasRefNO = randomValue()


        if (sharedPreferenceUtil.getQRStoreID().isNotEmpty()) {
            storeId = sharedPreferenceUtil.getQRStoreID()
            sharedPreferenceUtil.setQRStoreID("")
        } else {
            storeId = sharedPreferenceUtil.getStoreID()
        }

        if (mainMerchantId.isNullOrEmpty()) {
            mainMerchantId = sharedPreferenceUtil.getMerchantID()
        }

        if (sharedPreferenceUtil.getQRReNo().isNotEmpty()) {
            receiptNo = sharedPreferenceUtil.getQRReNo()
            sharedPreferenceUtil.setQRReNo("")
        } else {
            receiptNo = (100..999).shuffled().last().toString()
        }

        if (sharedPreferenceUtil.getQRPosType().isNotEmpty()) {
            posType = sharedPreferenceUtil.getQRPosType()
            sharedPreferenceUtil.setQRPosType("")
        } else {
            posType = sharedPreferenceUtil.getUserRole()
        }

        if (sharedPreferenceUtil.getQRPosID().isNotEmpty()) {
            posId = sharedPreferenceUtil.getQRPosID()
            sharedPreferenceUtil.setQRPosID("")
        } else {
            posId = sharedPreferenceUtil.getUserID()
        }

        if (sharedPreferenceUtil.getQRDistID().isNotEmpty()) {
            distributorId = sharedPreferenceUtil.getQRDistID()
            sharedPreferenceUtil.setQRDistID("")
        } else {
            distributorId = sharedPreferenceUtil.getDistributorID()
        }

        if (sharedPreferenceUtil.getQRUser().isNotEmpty()) {
            userId = sharedPreferenceUtil.getQRUser()
            sharedPreferenceUtil.setQRUser("")
        } else {
            userId = sharedPreferenceUtil.getUserID()
        }

        if (sharedPreferenceUtil.getQRMerEmail().isNotEmpty()) {
            merchantEmail = sharedPreferenceUtil.getQRMerEmail()
            sharedPreferenceUtil.setQRMerEmail("")
        } else {
            merchantEmail = sharedPreferenceUtil.getEmailID()
        }

        if (sharedPreferenceUtil.getQRCPhone().isNotEmpty()) {
            customerPhone = sharedPreferenceUtil.getQRCPhone()
            sharedPreferenceUtil.setQRCPhone("")
        } else {
            customerPhone = sharedPreferenceUtil.getMerchantMobileNumber()
        }

        if (sharedPreferenceUtil.getQRCEmail().isNotEmpty()) {
            customerEmail = sharedPreferenceUtil.getQRCEmail()
            sharedPreferenceUtil.setQRCEmail("")
        } else {
            customerEmail = sharedPreferenceUtil.getEmailID()
        }

        if (sharedPreferenceUtil.getQRTrNo().isNotEmpty()) {
            trnNo = sharedPreferenceUtil.getQRTrNo()
            sharedPreferenceUtil.setQRTrNo("")
        } else {
            trnNo = ContextUtils.randomValue().toString()
        }

        if (sharedPreferenceUtil.getQRPInvoiceNo().isNotEmpty()) {
            payrowInvoiceNo = sharedPreferenceUtil.getQRPInvoiceNo()
            sharedPreferenceUtil.setQRPInvoiceNo("")
        } else {
            payrowInvoiceNo = ContextUtils.randomValue().toString()
        }

        if (sharedPreferenceUtil.getQRTransURL().isNotEmpty()) {
            bankTransferURL = sharedPreferenceUtil.getQRTransURL()
            sharedPreferenceUtil.setQRTransURL("")
        }

        if (sharedPreferenceUtil.getQRCheckID().isNotEmpty()) {
            checkoutID = sharedPreferenceUtil.getQRCheckID()
            sharedPreferenceUtil.setQRCheckID("")
        }

        if (sharedPreferenceUtil.getQRCName().isNotEmpty()) {
            customerName = sharedPreferenceUtil.getQRCName()
            sharedPreferenceUtil.setQRCName("")
        } else {
            customerName = sharedPreferenceUtil.getMerchantName()
        }

        if (sharedPreferenceUtil.getQRCCity().isNotEmpty()) {
            customerBillingCity = sharedPreferenceUtil.getQRCCity()
            sharedPreferenceUtil.setQRCCity("")
        } else {
            customerBillingCity = sharedPreferenceUtil.getCity()
        }

        if (sharedPreferenceUtil.getQRCState().isNotEmpty()) {
            customerBillingState = sharedPreferenceUtil.getQRCState()
            sharedPreferenceUtil.setQRCState("")
        } else {
            customerBillingState = sharedPreferenceUtil.getCity()
        }

        if (sharedPreferenceUtil.getQRCCountry().isNotEmpty()) {
            customerBillingCountry = sharedPreferenceUtil.getQRCCountry()
            sharedPreferenceUtil.setQRCCountry("")
        } else {
            customerBillingCountry = sharedPreferenceUtil.getCountry()
        }

        if (sharedPreferenceUtil.getQRCPCode().isNotEmpty()) {
            customerBillingPostalCode = sharedPreferenceUtil.getQRCPCode()
            sharedPreferenceUtil.setQRCPCode("")
        } else {
            customerBillingPostalCode = sharedPreferenceUtil.getBOBox()
        }

        channel = if (paymentTypeChan == "scan qr") {
            "Third Party QRCode"
        } else {
            Constants.CARD// preference?.getString(Constants.TRANSACTION_TYPE, "")!!
        }

        val orderRequest = OrderRequest(
            null,
            orderNumber!!,
            Constants.CARD,
            mainMerchantId!!,
            null,
            null,
            customerEmail!!,
            customerPhone!!,
            null,
            null,
            userId!!,
            null,
            null,
            null,
            sharedPreferenceUtil.getMerchantEmail(),
            sharedPreferenceUtil.getMerchantPhone(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            vasRefNO.toString(),
            "Cancelled",
            customerName,
            sharedPreferenceUtil.getMerchantEmail(),
            sharedPreferenceUtil.getMerchantPhone(),
            customerBillingCity,
            customerBillingState,
            customerBillingCountry,
            customerBillingPostalCode,
            checkoutID,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            code.toString(),
            "Purchase",
            null,
            null,
            "Card Declined: $message",
            null,
            sharedPreferenceUtil.getTerminalID(),
            null,
            null,
            sharedPreferenceUtil.getBussinessId(),
            null,
            null,
            "Cancelled",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            4,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            HeaderSignatureUtil.getDeviceSN(), null, null, null, null,null
        )

        val bundle1 = Bundle()
        bundle1.putString("TYPE", "TAPTOPAY")
        bundle1.putString("INVOICENO", orderNumber)
        //  bundle1.putString("hostRefNO", hostRefNO.toString())
        bundle1.putString("vasRefNO", vasRefNO.toString())
        bundle1.putString("status", "Cancelled")
        bundle1.putString("paymentType", channel)
        bundle1.putString("bankTransferURL", bankTransferURL)
        bundle1.putBoolean("isPinBlock", isPinBlock)
        bundle1.putBoolean("SignatureStatus", signatureStatus)
        bundle1.putFloat("payRowVATAmount", payRowVATAmount)
        bundle1.putBoolean("payRowVATStatus", payRowVATStatus)
        bundle1.putString("totalAmount", totalAmount)
        bundle1.putString("payRowDigitFee", payRowDigitFee)
        icProcessActivity.startActivity(
            Intent(
                icProcessActivity,
                PaymentConfirmationActivity::class.java
            ).putExtras(bundle1)
                .putExtra("orderRequest", orderRequest as Serializable)
        )
       // icProcessActivity.finish()
    }

    fun callConfirmationActivity(
        signatureStatus: Boolean, isPinBlock: Boolean,
        icProcessActivity: ICProcessActivity,
        orderRequest: OrderRequest,
        cardNumber: String?,
        orderNumber: String?,
        hostRefNo: String?,
        vasRefNo: String?,
        status: String?,
        channel: String?,
        bankTransferURL: String?,
        authorizationId: String?,
        appPANSeqNo: String?
    ) {
        val bundle1 = Bundle()
        bundle1.putString("TYPE", "TAPTOPAY")
        bundle1.putString("CARDNO", cardNumber)
        bundle1.putString("INVOICENO", orderNumber)
        bundle1.putString("hostRefNO", hostRefNo)
        bundle1.putString("vasRefNO", vasRefNo)
        bundle1.putString("status", status)
        bundle1.putString("paymentType", channel)
        bundle1.putString("bankTransferURL", bankTransferURL)
        bundle1.putString("authCode", authorizationId)
        bundle1.putString("panSequenceNo", appPANSeqNo)
        bundle1.putBoolean("isPinBlock", isPinBlock)
        bundle1.putBoolean("SignatureStatus", signatureStatus)
        bundle1.putFloat("payRowVATAmount", orderRequest.vatAmount!!)
        bundle1.putBoolean("payRowVATStatus", orderRequest.vatStatus!!)
        bundle1.putString("totalAmount", orderRequest.amount.toString())
        bundle1.putString("payRowDigitFee", orderRequest.payRowDigitialFee.toString())
        // amount = ""
        icProcessActivity.startActivity(
            Intent(
                icProcessActivity,
                PaymentConfirmationActivity::class.java
            ).putExtras(bundle1)
                .putExtra("orderRequest", orderRequest as Serializable)
        )
     //   icProcessActivity.finish()
    }

    fun callPartialActivity(
        signatureStatus: Boolean, isPinBlock: Boolean,
        icProcessActivity: ICProcessActivity,
        orderRequest: OrderRequest,
        cardNumber: String?,
        orderNumber: String?,
        hostRefNo: String?,
        vasRefNo: String?,
        status: String?,
        channel: String?,
        bankTransferURL: String?,
        authorizationId: String?,
        appPANSeqNo: String?
    ) {
        val bundle1 = Bundle()
        bundle1.putString("TYPE", "TAPTOPAY")
        bundle1.putString("CARDNO", cardNumber)
        bundle1.putString("INVOICENO", orderNumber)
        bundle1.putString("hostRefNO", hostRefNo)
        bundle1.putString("vasRefNO", vasRefNo)
        bundle1.putString("status", status)
        bundle1.putString("paymentType", channel)
        bundle1.putString("bankTransferURL", bankTransferURL)
        bundle1.putString("authCode", authorizationId)
        bundle1.putString("panSequenceNo", appPANSeqNo)
        bundle1.putBoolean("isPinBlock", isPinBlock)
        bundle1.putBoolean("SignatureStatus", signatureStatus)
        bundle1.putString("totalAmount", orderRequest.amount.toString())
        bundle1.putString("payRowDigitFee", orderRequest.payRowDigitialFee.toString())
        // amount = ""
        icProcessActivity.startActivity(
            Intent(
                icProcessActivity,
                PartialPaymentActivity::class.java
            ).putExtras(bundle1)
                .putExtra("orderRequest", orderRequest as Serializable)
        )
        icProcessActivity.finish()
    }

    private var orderResponse = MutableLiveData<OrderResponse>()

    fun getOrderData(): MutableLiveData<OrderResponse> {
        return orderResponse
    }

    /*fun addOrder(
        context: Context, orderRequest: OrderRequest, paymentType: String?,
        bankTransferURL: String?
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            CardReceiptActivityRepository.getOrderMutableLiveData(context, orderRequest)
            orderResponse = CardReceiptActivityRepository.getOrderLiveData()

            if (paymentType == "Third Party QRCode") {
                orderResponse.value?.let {
                    postQR(
                        context,
                        it,
                        bankTransferURL!!
                    )
                }
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }*/

    /*   private fun postQR(context: Context, jsonObject: OrderResponse, url: String) {
           if (ContextUtils.isNetworkConnected(context)) {
               EnterAmountToPayCashRepository.postQRResponseMutableLiveData(context, jsonObject, url)
           } else {
               Toast.makeText(
                   context,
                   context.getString(R.string.internetnotavailable),
                   Toast.LENGTH_LONG
               ).show()
           }
       }*/

    private var purchaseResponse = MutableLiveData<PurchaseResponse>()

    fun purchaseResponse(): MutableLiveData<PurchaseResponse> {
        return purchaseResponse
    }

    private fun sendPurchaseRequest(
        onlineStatus: Boolean,
        transactionCallback: TransactionCallback,
        context: Context,
        purchaseRequest: PurchaseRequest,
        orderRequest: OrderRequest, channel: String,
        bankTransferURL: String?, pinAvaible: Boolean, signatureStatus: Boolean
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            PaymentActivityRepository.sendPurchaseRequestAPI(
                onlineStatus,
                transactionCallback,
                context,
                purchaseRequest,
                orderRequest,
                channel,
                bankTransferURL,
                pinAvaible, signatureStatus
            )
            purchaseResponse = PaymentActivityRepository.getPurchaseResponse()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun reSetPurchaseResp() {
        purchaseResponse.value = null
    }


    fun cancelTransaction(
        orderedNumber: String,
        context: Context,
        payRowDigitFee: String,
        amount: String, payRowVATStatus: Boolean, payRowVATAmount: Float,
        signatureStatus: Boolean, isPinBlock: Boolean,
        totalAmount: String,
        paymentTypeChan: String,
        /*purchaseDetails: PurchaseBreakdownDetails,*/
        icProcessActivity: ICProcessActivity, sharedPreferenceUtil: SharedPreferenceUtil
    ) {

        TRANSACTION_TYPE = sharedPreferenceUtil.getTransactionType()

        orderNumber = orderedNumber

        //prepare order
        val calendar = Calendar.getInstance().time
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        currentDate = sdf.format(calendar)

        //val receiptNO = (100..999).shuffled().last()

        vasRefNO = randomValue()


        if (sharedPreferenceUtil.getQRStoreID().isNotEmpty()) {
            storeId = sharedPreferenceUtil.getQRStoreID()
            sharedPreferenceUtil.setQRStoreID("")
        } else {
            storeId = sharedPreferenceUtil.getStoreID()
        }

        if (mainMerchantId.isNullOrEmpty()) {
            mainMerchantId = sharedPreferenceUtil.getMerchantID()
        }

        if (sharedPreferenceUtil.getQRReNo().isNotEmpty()) {
            receiptNo = sharedPreferenceUtil.getQRReNo()
            sharedPreferenceUtil.setQRReNo("")
        } else {
            receiptNo = (100..999).shuffled().last().toString()
        }

        if (sharedPreferenceUtil.getQRPosType().isNotEmpty()) {
            posType = sharedPreferenceUtil.getQRPosType()
            sharedPreferenceUtil.setQRPosType("")
        } else {
            posType = sharedPreferenceUtil.getUserRole()
        }

        if (sharedPreferenceUtil.getQRPosID().isNotEmpty()) {
            posId = sharedPreferenceUtil.getQRPosID()
            sharedPreferenceUtil.setQRPosID("")
        } else {
            posId = sharedPreferenceUtil.getUserID()
        }

        if (sharedPreferenceUtil.getQRDistID().isNotEmpty()) {
            distributorId = sharedPreferenceUtil.getQRDistID()
            sharedPreferenceUtil.setQRDistID("")
        } else {
            distributorId = sharedPreferenceUtil.getDistributorID()
        }

        if (sharedPreferenceUtil.getQRUser().isNotEmpty()) {
            userId = sharedPreferenceUtil.getQRUser()
            sharedPreferenceUtil.setQRUser("")
        } else {
            userId = sharedPreferenceUtil.getUserID()
        }

        if (sharedPreferenceUtil.getQRMerEmail().isNotEmpty()) {
            merchantEmail = sharedPreferenceUtil.getQRMerEmail()
            sharedPreferenceUtil.setQRMerEmail("")
        } else {
            merchantEmail = sharedPreferenceUtil.getEmailID()
        }

        if (sharedPreferenceUtil.getQRCPhone().isNotEmpty()) {
            customerPhone = sharedPreferenceUtil.getQRCPhone()
            sharedPreferenceUtil.setQRCPhone("")
        } else {
            customerPhone = sharedPreferenceUtil.getMerchantMobileNumber()
        }

        if (sharedPreferenceUtil.getQRCEmail().isNotEmpty()) {
            customerEmail = sharedPreferenceUtil.getQRCEmail()
            sharedPreferenceUtil.setQRCEmail("")
        } else {
            customerEmail = sharedPreferenceUtil.getEmailID()
        }

        if (sharedPreferenceUtil.getQRTrNo().isNotEmpty()) {
            trnNo = sharedPreferenceUtil.getQRTrNo()
            sharedPreferenceUtil.setQRTrNo("")
        } else {
            trnNo = ContextUtils.randomValue().toString()
        }

        if (sharedPreferenceUtil.getQRPInvoiceNo().isNotEmpty()) {
            payrowInvoiceNo = sharedPreferenceUtil.getQRPInvoiceNo()
            sharedPreferenceUtil.setQRPInvoiceNo("")
        } else {
            payrowInvoiceNo = ContextUtils.randomValue().toString()
        }

        if (sharedPreferenceUtil.getQRTransURL().isNotEmpty()) {
            bankTransferURL = sharedPreferenceUtil.getQRTransURL()
            sharedPreferenceUtil.setQRTransURL("")
        }

        if (sharedPreferenceUtil.getQRCheckID().isNotEmpty()) {
            checkoutID = sharedPreferenceUtil.getQRCheckID()
            sharedPreferenceUtil.setQRCheckID("")
        }

        if (sharedPreferenceUtil.getQRCName().isNotEmpty()) {
            customerName = sharedPreferenceUtil.getQRCName()
            sharedPreferenceUtil.setQRCName("")
        } else {
            customerName = sharedPreferenceUtil.getMerchantName()
        }

        if (sharedPreferenceUtil.getQRCCity().isNotEmpty()) {
            customerBillingCity = sharedPreferenceUtil.getQRCCity()
            sharedPreferenceUtil.setQRCCity("")
        } else {
            customerBillingCity = sharedPreferenceUtil.getCity()
        }

        if (sharedPreferenceUtil.getQRCState().isNotEmpty()) {
            customerBillingState = sharedPreferenceUtil.getQRCState()
            sharedPreferenceUtil.setQRCState("")
        } else {
            customerBillingState = sharedPreferenceUtil.getCity()
        }

        if (sharedPreferenceUtil.getQRCCountry().isNotEmpty()) {
            customerBillingCountry = sharedPreferenceUtil.getQRCCountry()
            sharedPreferenceUtil.setQRCCountry("")
        } else {
            customerBillingCountry = sharedPreferenceUtil.getCountry()
        }

        if (sharedPreferenceUtil.getQRCPCode().isNotEmpty()) {
            customerBillingPostalCode = sharedPreferenceUtil.getQRCPCode()
            sharedPreferenceUtil.setQRCPCode("")
        } else {
            customerBillingPostalCode = sharedPreferenceUtil.getBOBox()
        }

        channel = if (paymentTypeChan == "scan qr") {
            "Third Party QRCode"
        } else {
            Constants.CARD// preference?.getString(Constants.TRANSACTION_TYPE, "")!!
        }

        val orderRequest = OrderRequest(
            storeId!!,
            orderNumber!!,
            Constants.CARD,
            mainMerchantId!!,
            posId!!,
            posType!!,
            customerEmail!!,
            customerPhone!!,
            distributorId!!,
            null,
            userId!!,
            null,
            null,
            null,
            sharedPreferenceUtil.getMerchantEmail(),
            sharedPreferenceUtil.getMerchantPhone(),
            trnNo,
            receiptNo,
            null,
            payrowInvoiceNo,
            null,
            null,
            null,
            null,
            vasRefNO.toString(),
            "Closed",
            customerName,
            sharedPreferenceUtil.getMerchantEmail(),
            sharedPreferenceUtil.getMerchantPhone(),
            customerBillingCity,
            customerBillingState,
            customerBillingCountry,
            customerBillingPostalCode,
            checkoutID,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "Purchase",
            null,
            null,
            "Closed",
            null,
            sharedPreferenceUtil.getTerminalID(),
            customerEmail,
            customerPhone,
            sharedPreferenceUtil.getBussinessId(),
            null,
            null,
            "Closed",
            amount.toFloat(),
            null,
            null,
            null,
            null,
            null,
            null,
            TRANSACTION_TYPE,
            null,
            null,
            null,
            null,
            sharedPreferenceUtil.getReportID(),
            signatureStatus,
            isPinBlock,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            HeaderSignatureUtil.getDeviceSN(), null, payRowVATStatus, payRowVATAmount, null,null
        )

        CardReceiptActivityRepository.getOrderMutableLiveData(
            context,
            orderRequest,
            channel,
            bankTransferURL
        )

        CardReceiptActivityRepository.getOrderLiveData().observeOnce(icProcessActivity) {
            val intent = Intent(context, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            icProcessActivity.startActivity(intent)
            icProcessActivity.finish()
        }
        /* val bundle1 = Bundle()
         bundle1.putString("TYPE", "TAPTOPAY")
         bundle1.putString("INVOICENO", orderNumber)
         //  bundle1.putString("hostRefNO", hostRefNO.toString())
         bundle1.putString("vasRefNO", vasRefNO.toString())
         bundle1.putString("status", "Cancelled")
         bundle1.putString("paymentType", channel)
         bundle1.putString("bankTransferURL", bankTransferURL)
         bundle1.putBoolean("isPinBlock", isPinBlock)
         bundle1.putBoolean("SignatureStatus", signatureStatus)
         bundle1.putFloat("payRowVATAmount", payRowVATAmount)
         bundle1.putBoolean("payRowVATStatus", payRowVATStatus)
         bundle1.putString("totalAmount", totalAmount)
         bundle1.putString("payRowDigitFee", payRowDigitFee)
         icProcessActivity.startActivity(
             Intent(
                 icProcessActivity,
                 PaymentConfirmationActivity::class.java
             ).putExtras(bundle1)
                 .putExtra("orderRequest", orderRequest as Serializable)
         )
         icProcessActivity.finish()*/

    }


    private fun getCardBrand(aid: String?): String {
        return when (aid) {
            "A0000000031010", "A0000000032010" -> {
                "VISA"
            }
            "A0000000041010", "A0000000042010" -> {
                "MASTERCARD"
            }
            "A0000000043060" -> {
                "MAESTRO"
            }
            else -> ""
        }
    }

    private fun prepareFeeReq(
        expiryDate: String,
        totalAmount: String,
        cardBrand: String?,
        feeServiceDataList: ArrayList<FeeServiceData>,
        purchaseRequest: PurchaseRequest
    ): PurchaseRequest {
         pmtTxnRefCode = getTXNRefCODE()
        val fssFeeFetchReqest = FSSFeeFetchReqest(
            "2", totalAmount, "0000", getCorelationId(), "MPAY",
            pmtTxnRefCode, cardBrand, getCorelationId(), getCorelationId(), null,feeServiceDataList
        )


        val feeFetchString = Gson().toJson(fssFeeFetchReqest)

        val purchaseFeeRequest = PurchaseRequest(purchaseRequest.cardNumber)
        purchaseFeeRequest.MTI = "0100"
        purchaseFeeRequest.processingCode = "480000"//purchaseRequest.processingCode
        purchaseFeeRequest.amount = purchaseRequest.amount
        purchaseFeeRequest.transDateAndTime = purchaseRequest.transDateAndTime

        purchaseFeeRequest.systemTraceAuditNumber = purchaseRequest.systemTraceAuditNumber
        purchaseFeeRequest.time = purchaseRequest.time
        purchaseFeeRequest.date = purchaseRequest.date
        purchaseFeeRequest.expiryDate = expiryDate

        purchaseFeeRequest.merchantType = purchaseRequest.merchantType
        purchaseFeeRequest.posEntryMode = purchaseRequest.posEntryMode
        purchaseFeeRequest.posConditionCode = "51"//purchaseRequest.posConditionCode
        purchaseFeeRequest.cardAcceptorTerminalID = purchaseRequest.cardAcceptorTerminalID

        purchaseFeeRequest.cardAcceptorIDCode = purchaseRequest.cardAcceptorIDCode
        purchaseFeeRequest.cardAcceptorNameLocation = purchaseRequest.cardAcceptorNameLocation

       // purchaseFeeRequest.cardAcceptorTerminalID = purchaseRequest.cardAcceptorTerminalID
        purchaseFeeRequest.currencyCode = purchaseRequest.currencyCode
        purchaseFeeRequest.fssFetch = feeFetchString.toString()
        // "{\"curExponent\":\"2\",\"txnAmount\":\"620.00\",\"tranType\":\"0000\",\"refId\":\"935110000001\",\"correlationId\":\"635110000001\",\"schemeId\":\"MPAY\",\"pmtTxnRefCode\":\"201935166561122\",\"cardType\":\"VISA_ONUS\",\"merchantTrn\":\"782304000\",\"serviceTrn\":\"501935166562\",\"serviceData\":[{\"serviceId\":\"7823001\",\"servAmount\":\"300\",\"merchantId\":\"782304000\",\"serviceType\":\"D\",\"noOfTransactions\":\"1\"},{\"serviceId\":\"7823002\",\"servAmount\":\"300\",\"merchantId\":\"782304000\",\"serviceType\":\"D\",\"noOfTransactions\":\"1\"},{\"serviceId\":\"78231001\",\"servAmount\":\"10\",\"merchantId\":\"782314000\",\"serviceType\":\"D\",\"noOfTransactions\":\"1\"},{\"serviceId\":\"78231002\",\"servAmount\":\"10\",\"merchantId\":\"782314000\",\"serviceType\":\"D\",\"noOfTransactions\":\"1\"}]}"
        return purchaseFeeRequest
    }

    private fun getTXNRefCODE(): String {
        return randomValue().toString() + randomValue().toString() + randomValue().toString()
    }

    private fun getCorelationId(): String {
        return randomValue().toString() + randomValue().toString() + (10..99).shuffled().last()
            .toString()
    }
}
