package com.payment.payrowapp.refundandreversal

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


object RefundRepository {
    val mutableLiveData = MutableLiveData<RefundResponse>()
    val orderResponseLiveData = MutableLiveData<OrderResponse>()

    fun getOrderMutableLiveData(
        context: Context,
        orderRequest: RefOrderRequest
    ): MutableLiveData<OrderResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(orderRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("PUT", "/api/orders", timeStamp, context)

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
                "PUT",
                "/api/orders",
                jsonString2
            ),
            context
        )
        ApiClient.apiService.refOrder(headerMap, encryptDataClass)
            .enqueue(object :
                Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    Log.d("Request", "req->" + call.request().body.toString())
                    if (response.isSuccessful) {

                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val orderResponse = Gson().fromJson(
                            decrypt.toString(),
                            OrderResponse::class.java
                        )
                        orderResponseLiveData.postValue(orderResponse)

                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error) + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    Toast.makeText(context, context.getString(R.string.failure) + context.getString(R.string.service_unavialable), Toast.LENGTH_LONG)
                        .show()
                }
            })

        return orderResponseLiveData
    }

    fun getOrderLiveData(): MutableLiveData<OrderResponse> {
        return orderResponseLiveData
    }
}