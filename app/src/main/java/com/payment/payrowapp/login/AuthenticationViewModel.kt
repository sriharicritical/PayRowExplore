package com.payment.payrowapp.login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.otp.EnterOTPActivity
import com.payment.payrowapp.refundandreversal.RefundConfirmationActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONException
import org.json.JSONObject

class AuthenticationViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<DecryptResponse>()
    private var verifyOTPResponse = MutableLiveData<DecryptResponse>()
    private var verifyAuthCodeResponse = MutableLiveData<DecryptResponse>()

    fun getData(): MutableLiveData<DecryptResponse> {
        return response
    }

    fun getVerifyOTPData(): MutableLiveData<DecryptResponse> {
        return verifyOTPResponse
    }

    fun getVerifyAuthData(): MutableLiveData<DecryptResponse> {
        return verifyAuthCodeResponse
    }

    fun getOTPResponse(otpRequest: OTPRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.getMutableLiveData(context, otpRequest)
            response = AuthenticationRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun verifyOTPResponse(otpRequest: VerifyOTPRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.verifyOTPMutableLiveData(context, otpRequest)
            verifyOTPResponse = AuthenticationRepository.getVerifyOTPLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun verifyAuthCodeMutableLiveData(
        authCodeRequest: AuthCodeRequest,
        sharedPreferenceUtil: SharedPreferenceUtil
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.verifyAuthCodeMutableLiveData(
                context,
                authCodeRequest,
                sharedPreferenceUtil
            )
            verifyAuthCodeResponse = AuthenticationRepository.getVerifyAuthLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getLoginOTPResponse(
        loginOTPRequest: LoginOTPRequest,
        sharedPreferenceUtil: SharedPreferenceUtil
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.getLoginMutableLiveData(
                context,
                loginOTPRequest,
                sharedPreferenceUtil
            )
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
        //response = AuthenticationRepository.getLiveData()
    }

    private var purchaseResponse = MutableLiveData<PurchaseResponse>()


    fun sendPurchaseRequest(
        cardNumber: String,
        vasRefNo: String,
        mode: String,
        orderNumber: String,
        context: Context,
        purchaseRequest: PurchaseRequest
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.sendPurchaseRequestAPI(
                false,
                cardNumber,
                vasRefNo,
                mode,
                orderNumber,
                context,
                purchaseRequest
            )
            purchaseResponse = AuthenticationRepository.getPurchaseResponse()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun purchaseResponse(): MutableLiveData<PurchaseResponse> {
        return purchaseResponse
    }

    fun ecommVoidRequest(
        mode: String,
        orderNumber: String,
        context: Context
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.setEcommerceVoid(mode, orderNumber, context)
            ecommVoidResponse = AuthenticationRepository.getEcommerceVoid()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun ecommVoidResp(): MutableLiveData<EcommerceVoidResponse> {
        return ecommVoidResponse
    }

    private var ecommVoidResponse = MutableLiveData<EcommerceVoidResponse>()

    fun ecommRefundRequest(
        mode: String,
        orderNumber: String,
        context: Context
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.setEcommerceRefund(mode, orderNumber, context)
            ecommRefundResponse = AuthenticationRepository.getEcommerceRefund()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun ecommReffundResp(): MutableLiveData<EcommerceVoidResponse> {
        return ecommRefundResponse
    }

    private var ecommRefundResponse = MutableLiveData<EcommerceVoidResponse>()


    fun prepareFeeReq(
        amount: String,
        purchaseRequest: PurchaseRequest, feeFetchString: String
    ): PurchaseRequest {

        val purchaseFeeRequest = PurchaseRequest(purchaseRequest.cardNumber)
        purchaseFeeRequest.MTI = "0100"
        purchaseFeeRequest.processingCode = "480000"//purchaseRequest.processingCode
        purchaseFeeRequest.amount = getHexDecimalValue(amount)//purchaseRequest.amount
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
        purchaseFeeRequest.fssFetch = feeFetchString
        return purchaseFeeRequest
    }

    private var feeRefundResponse = MutableLiveData<FeePrepareResponse>()

    fun getRefundData(): MutableLiveData<FeePrepareResponse> {
        return feeRefundResponse
    }

    fun getRefundFeeResponse(refundFeeRequest: RefundFeeRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.getFeeRefundAPI(context, refundFeeRequest)
            feeRefundResponse = AuthenticationRepository.getFeeRefundResponse()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var feeFSSResponse = MutableLiveData<PurchaseResponse>()
    fun getFSSFeeData(): MutableLiveData<PurchaseResponse> {
        return feeFSSResponse
    }

    fun reSetPurchaseResp() {
        feeFSSResponse.value = null
        purchaseResponse.value = null
    }

    private fun getFSSFeeResponse(status: Boolean, purchaseRequest: PurchaseRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.getFSSFeeAPI(context, purchaseRequest, status)
            feeFSSResponse = AuthenticationRepository.getFSSFeeResponse()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    fun initiateRefundTrans(
        cardNumber: String,
        vasRefNo: String,
        orderNumber: String,
        cardBrand: String,
        mode: String,
        purchaseRequest: PurchaseRequest,
        purchaseFeeReq: PurchaseRequest,
        enterOTPActivity: EnterOTPActivity
    ) {
        val progressDialog = ProgressDialog(enterOTPActivity)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        getFSSFeeResponse(true, purchaseFeeReq)

        getFSSFeeData().observeOnce(enterOTPActivity) { purchaseResult ->
            if (purchaseResult?.purchaseResult?.responseCode == "00") {
                var feeResultObject: JSONObject? = null
                try {
                    feeResultObject =
                        JSONObject(purchaseResult.purchaseResult.fssFetch)
                    val totalChargAmount =
                        feeResultObject.getString("totalChargableAmount")
                    purchaseRequest.amount =
                        getHexDecimalValue(
                            totalChargAmount
                        )
                    purchaseRequest.orderNo =
                        purchaseResult.purchaseResult.orderNo
                    purchaseFeeReq.orderNo =
                        purchaseResult.purchaseResult.orderNo
                    sendPurchaseRequest(
                        cardNumber,
                        vasRefNo,
                        mode,
                        orderNumber,
                        enterOTPActivity,
                        purchaseRequest
                    )
                    purchaseResponse().observeOnce(enterOTPActivity)
                    { purchaseResult1 ->
                        progressDialog.dismiss()
                        val ackPurchaseRequest =
                            prepareACKRequest(
                                purchaseResult.purchaseResult.fssFetch,
                                purchaseResult1.purchaseResult.responseCode,
                                purchaseResult1.purchaseResult.authorizationId,
                                purchaseFeeReq
                            )
                        purchaseFeeReq.amount = purchaseRequest.amount
                        purchaseFeeReq.fssFetch = ackPurchaseRequest
                        purchaseFeeReq.posConditionCode = "52"
                        purchaseFeeReq.processingCode = "490000"
                        purchaseFeeReq.ackCardBrand = cardBrand
                        getFSSFeeResponse(
                            false,
                            purchaseFeeReq
                        )
                        val intent = Intent(
                            enterOTPActivity,
                            RefundConfirmationActivity::class.java
                        )
                        if (purchaseResult1.purchaseResult.responseCode == "00") {
                            intent.putExtra(
                                "authCode",
                                purchaseResult1.purchaseResult.authorizationId
                            )
                            intent.putExtra("status", "REFUNDED")
                            intent.putExtra(
                                "hostRefNO",
                                purchaseResult1.purchaseResult.orderNo
                            )
                            intent.putExtra(
                                "reference48",
                                purchaseResult.purchaseResult.fssFetch
                            )
                        } else {
                            intent.putExtra("status", "NOT REFUNDED")
                        }
                        intent.putExtra("vasRefNO", vasRefNo)
                        intent.putExtra("CARDNO", cardNumber)
                        intent.putExtra("orderNumber", orderNumber)
                        intent.putExtra("mode", mode)
                        intent.putExtra(
                            "responseCode",
                            purchaseResult1.purchaseResult.responseCode
                        )
                        intent.putExtra("cardBrand", cardBrand)
                        intent.putExtra("totalAmount", totalChargAmount)
                        reSetPurchaseResp()
                        enterOTPActivity.startActivity(intent)
                        enterOTPActivity.finish()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                progressDialog.dismiss()
                val intent =
                    Intent(enterOTPActivity, RefundConfirmationActivity::class.java)
                intent.putExtra("status", "Cancelled")
                intent.putExtra("vasRefNO", vasRefNo)
                intent.putExtra("CARDNO", cardNumber)
                intent.putExtra("orderNumber", orderNumber)
                intent.putExtra("mode", mode)
                enterOTPActivity.startActivity(intent)
                enterOTPActivity.finish()
            }
        }
    }

    companion object {

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

        @JvmStatic
        fun getHexDecimalValue(amount: String): String {
            var amountValue = amount.toFloat()
            amountValue *= 100
            val fAmount = (amountValue + 0.1).toInt()
            return getHexaDecimal(fAmount.toString())
        }

        @JvmStatic
        fun prepareACKRequest(
            feeFetchString: String,
            authRespCode: String,
            autID: String?,
            purchaseFeeRequest: PurchaseRequest
        ): String {
            val feeResultObject = JSONObject(feeFetchString)
            val fssFeeFetchReqest = Gson().fromJson(
                purchaseFeeRequest.fssFetch,
                FSSFeeFetchReqest::class.java
            )

            val txnStatus = if (authRespCode == "00") {
                "04"
            } else {
                "01"
            }
            val acknoledgemenrRequest = AcknoledgemenrRequest(
                feeResultObject.getString("totalChargableAmount"),
                fssFeeFetchReqest.tranType,
                fssFeeFetchReqest.correlationId,
                fssFeeFetchReqest.schemeId,
                purchaseFeeRequest.orderNo,
                ContextUtils.postDate(),
                authRespCode,
                autID,
                txnStatus,
                feeResultObject.getString("refId"),
                ContextUtils.acqTransactionCompletionDate(),
                purchaseFeeRequest.orderNo,
                "CC"
            )

            return Gson().toJson(acknoledgemenrRequest)
        }
    }


    fun getMIDOTPResponse(otpRequest: OTPRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.getMIDMutableLiveData(context, otpRequest)
            midResponse = AuthenticationRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getMIDData(): MutableLiveData<DecryptResponse> {
        return midResponse
    }

    private var midResponse = MutableLiveData<DecryptResponse>()


    fun getMIDOTPVerifyResponse(service: String, status: Boolean, otpRequest: VerifyOTPRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            AuthenticationRepository.verifyMIDOTPMutableLiveData(
                service,
                status,
                context,
                otpRequest
            )
            midVerifyResponse = AuthenticationRepository.getMIDVerifyLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getMIDVerifyData(): MutableLiveData<DecryptResponse> {
        return midVerifyResponse
    }

    private var midVerifyResponse = MutableLiveData<DecryptResponse>()
}