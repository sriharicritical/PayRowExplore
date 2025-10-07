package com.payment.payrowapp.wizzit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.newpayment.PaymentConfirmationActivity
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.sunmipay.TransactionCallback
import com.payment.payrowapp.utils.ContextUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

object PaymentActivityRepository {
    val purchaseResponseResult = MutableLiveData<PurchaseResponse>()

    private val purchaseFeeResponseResult = MutableLiveData<PurchaseResponse>()

    fun navigateConfirmationScreen(
        context: Context,
        orderRequest: OrderRequest, channel: String, bankTransferURL: String?
    ) {
        val bundle1 = Bundle()
        bundle1.putString("TYPE", "TAPTOPAY")
        bundle1.putString("CARDNO", orderRequest.cardNumber)
        // bundle1.putString("INVOICENO", orderRequest.orderNumber)
        bundle1.putString("hostRefNO", orderRequest.hostReference)
        bundle1.putString("vasRefNO", orderRequest.vasReference)
        bundle1.putString("status", orderRequest.checkoutStatus)
        bundle1.putString("paymentType", channel)
        bundle1.putString("bankTransferURL", bankTransferURL)
        val intent = Intent(
            context,
            PaymentConfirmationActivity::class.java
        ).putExtras(bundle1).putExtra("orderRequest", orderRequest as Serializable)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(
            intent
        )
    }

    fun sendPurchaseRequestAPI(
        onlineStatus: Boolean,
        transactionCallback: TransactionCallback,
        context: Context, purchaseRequest: PurchaseRequest, orderRequest: OrderRequest,
        channel: String, bankTransferURL: String?, pinAvailable: Boolean, signatureStatus: Boolean
    ): MutableLiveData<PurchaseResponse> {


        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val jsonString = Gson().toJson(purchaseRequest)
        Log.v("send purchase api", jsonString)

        /* val encrypt = EncryptDecrypt.encrypt(
             jsonString!!,
             sharedPreferenceUtil.getAESKey(),
             sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlg()
         )

         val certReq = if (isPinBlock) {
             CertReq(
                 jsonString,
                 null
             )
         } else {
             CertReq(
                 encrypt,
                 RSAKeyPairGenerator.encryptData(context, sharedPreferenceUtil.getAESKey())
             )
         }*/

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
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
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

                            Log.v("decrypt", decrypt)
                            val purchaseResponse = Gson().fromJson(
                                decrypt.toString(),
                                PurchaseResponse::class.java
                            )
                            purchaseResponseResult.postValue(purchaseResponse)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        // purchaseResponseResult.postValue(null)
                        orderRequest.errorTracking = "Purchase:"
                        val failedRequstClass =
                            FailedRequstClass(
                                orderRequest,
                                channel,
                                bankTransferURL, onlineStatus, signatureStatus
                            )
                        transactionCallback.onTransactionFailed(failedRequstClass, pinAvailable)
                        // orderRequest.errorTracking = "Purchase :"
                        //  navigateConfirmationScreen(context, orderRequest, channel, bankTransferURL)
                        // getOrderMutableLiveData(context, orderRequest)
                        Toast.makeText(
                            context,
                            "Error: onResponse" + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    orderRequest.errorTracking = "Purchase:" + t.localizedMessage
                    val failedRequstClass =
                        FailedRequstClass(
                            orderRequest,
                            channel,
                            bankTransferURL, onlineStatus, signatureStatus
                        )
                    transactionCallback.onTransactionFailed(failedRequstClass, pinAvailable)
                    Toast.makeText(
                        context,
                        "Failure: onFailure" + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        return purchaseResponseResult
    }

    fun getPurchaseResponse(): MutableLiveData<PurchaseResponse> {
        return purchaseResponseResult
    }

    fun getFSSFeeAPI(
        status: Boolean,
        context: Context, purchaseRequest: PurchaseRequest, onlineStatus: Boolean,
        transactionCallback: TransactionCallback,
        orderRequest: OrderRequest, channel: String,
        bankTransferURL: String?, pinAvaible: Boolean, signatureStatus: Boolean
    ): MutableLiveData<PurchaseResponse> {

     /*   ApiClient.apiSoftPOS.fssFeeFetch(purchaseRequest)
            .enqueue(object : Callback<PurchaseResponse> {
                override fun onResponse(
                    call: Call<PurchaseResponse>,
                    response: Response<PurchaseResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        if (status) {
                            purchaseFeeResponseResult.postValue(response.body())
                        }
                    } else {

                        if (status) {
                            orderRequest.errorTracking = "Possibility:"
                            val failedRequstClass =
                                FailedRequstClass(
                                    orderRequest,
                                    channel,
                                    bankTransferURL, onlineStatus, signatureStatus
                                )
                            transactionCallback.onTransactionFailed(failedRequstClass, pinAvaible)
                        }
                        Toast.makeText(
                            context,
                            "Error: onResponse" + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PurchaseResponse>, t: Throwable) {

                    if (status) {
                        orderRequest.errorTracking = "Possibility:" + t.localizedMessage
                        val failedRequstClass =
                            FailedRequstClass(
                                orderRequest,
                                channel,
                                bankTransferURL, onlineStatus, signatureStatus
                            )
                        transactionCallback.onTransactionFailed(failedRequstClass, pinAvaible)
                    }
                }
            })*/

        return purchaseFeeResponseResult
    }

    fun getFSSFeeResponse(): MutableLiveData<PurchaseResponse> {
        return purchaseFeeResponseResult
    }

}