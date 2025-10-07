package com.payment.payrowapp.product

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
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

object ProductRepository {

    private val feeResponseResult = MutableLiveData<FeePrepareResponse>()

    fun getFeePrepareAPI(
        context: Context, feePrepareRequest: FeePrepareRequest,
    ): MutableLiveData<FeePrepareResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(feePrepareRequest)

        Log.v("decrypt", "req" + jsonString)
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/pos/purchase",
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
                "/mobileapis/pos/purchase",
                jsonString2
            ),
            context
        )

        ApiClient.apiSoftPOS.feePrepare(headerMap,encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val feePrepareResponse = Gson().fromJson(
                            decrypt.toString(),
                            FeePrepareResponse::class.java
                        )
                        feeResponseResult.postValue(feePrepareResponse)
                    } else {
                        Toast.makeText(
                            context,
                            "Error: onResponse" + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {

                }
            })

        return feeResponseResult
    }

    fun getFeeResponse(): MutableLiveData<FeePrepareResponse> {
        return feeResponseResult
    }

}