package com.payment.payrowapp.paymentlink

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
import com.payment.payrowapp.utils.LoaderCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object PaymentLinkRepository {
    val mutableLiveData = MutableLiveData<PaymentLinkResponse>()

    fun getMutableLiveData(
        context: Context,
        paymentLinkRequest: PaymentLinkRequest, loaderCallback: LoaderCallback
    ): MutableLiveData<PaymentLinkResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(paymentLinkRequest)

        Log.v("decrypt", "req" + jsonString)
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/purchaseorderlink",
                timeStamp,
                context
            )

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
                "/mobileapis/payrow/purchaseorderlink",
                jsonString2
            ),
            context
        )
        ApiClient.apiServiceGateway.getPaymentLink(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        try {

                            if (response.body()?.data != null) {

                                val decrypt = EncryptDecrypt.decrypt(
                                    response.body()?.data,
                                    secretKey,
                                    sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                                )
                                if (decrypt != null) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.link_created),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.v("decrypt", decrypt)
                                    val paymentLinkResponse = Gson().fromJson(
                                        decrypt.toString(),
                                        PaymentLinkResponse::class.java
                                    )
                                    mutableLiveData.postValue(paymentLinkResponse)
                                } else {
                                    loaderCallback.closeLoader()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.link_generation_failed),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                loaderCallback.closeLoader()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.link_generation_failed),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            loaderCallback.closeLoader()
                            e.printStackTrace()
                            Toast.makeText(
                                context,
                                context.getString(R.string.link_generation_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        loaderCallback.closeLoader()
                        Toast.makeText(
                            context,
                            context.getString(R.string.link_generation_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    loaderCallback.closeLoader()
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        return mutableLiveData
    }

    fun getLiveData(): MutableLiveData<PaymentLinkResponse> {
        return mutableLiveData
    }
}