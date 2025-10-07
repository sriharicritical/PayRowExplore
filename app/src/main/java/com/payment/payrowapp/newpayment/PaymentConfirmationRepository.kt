package com.payment.payrowapp.newpayment

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PaymentConfirmationRepository {


    fun reversalRequestAPI(
        orderRequest: OrderRequest, paymentType: String?, bankTransferURL: String?,
        context: Context, purchaseRequest: PurchaseRequest
    ): MutableLiveData<PurchaseResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val jsonString = Gson().toJson(purchaseRequest)

        Log.v("void request", jsonString)

        val timeStamp = HeaderSignatureUtil.getISO()
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/pos/purchase", timeStamp, context)

        // encrypt data
        val encryptData = EncryptDecrypt.encrypt(
            jsonString,
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )

        val encryptDataClass = EncryptDataClass(encryptData)
        val jsonString2 = Gson().toJson(encryptDataClass)

        //generate headers
        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/pos/purchase",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.doPurchase(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        try {
                            val decrypt = EncryptDecrypt.decrypt(
                                response.body()!!.data,
                                secretKey,
                                sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                            )

                            Log.v("reversal+decrypt", decrypt)
                            val purchaseResponse = Gson().fromJson(
                                decrypt.toString(),
                                PurchaseResponse::class.java
                            )
                            if (purchaseResponse.purchaseResult.responseCode == "00") {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.amount_reversal),
                                    Toast.LENGTH_LONG
                                ).show()

                                orderRequest.checkoutStatus = "REVERSAL"
                                orderRequest.orderStatus = "REVERSAL"
                                orderRequest.hostReference = purchaseResponse.purchaseResult.orderNo
                                orderRequest.responseCode =
                                    purchaseResponse.purchaseResult.responseCode
                                orderRequest.authorizationId =
                                    purchaseResponse.purchaseResult.authorizationId

                                CardReceiptActivityRepository.getOrderMutableLiveData(
                                    context,
                                    orderRequest,
                                    paymentType,
                                    bankTransferURL
                                )
                                /*val refOrderRequest = RefOrderRequest(
                                    orderNumber,
                                    "REVERSAL",
                                    "Purchase",
                                    purchaseResponse.purchaseResult.orderNo,
                                    purchaseResponse.purchaseResult.responseCode,
                                    purchaseResponse.purchaseResult.authorizationId,
                                    "REVERSAL",
                                    null, null
                                )
                                RefundRepository.getOrderMutableLiveData(
                                    context,
                                    refOrderRequest
                                )*/

                            } else {
                                if (orderRequest.orderStatus == "Terminal Declined") {
                                    orderRequest.checkoutStatus = "NOT REVERSAL"
                                    orderRequest.orderStatus = "NOT REVERSAL"
                                }

                                orderRequest.responseCode =
                                    purchaseResponse.purchaseResult.responseCode

                                CardReceiptActivityRepository.getOrderMutableLiveData(
                                    context,
                                    orderRequest,
                                    paymentType,
                                    bankTransferURL
                                )
                            }
                            reversalResponseResult.postValue(purchaseResponse)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        CardReceiptActivityRepository.getOrderMutableLiveData(
                            context,
                            orderRequest,
                            paymentType,
                            bankTransferURL
                        )
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    CardReceiptActivityRepository.getOrderMutableLiveData(
                        context,
                        orderRequest,
                        paymentType,
                        bankTransferURL
                    )
                }
            })

        return reversalResponseResult
    }

    fun getPurchaseResponse(): MutableLiveData<PurchaseResponse> {
        return reversalResponseResult
    }

    private val reversalResponseResult = MutableLiveData<PurchaseResponse>()
}