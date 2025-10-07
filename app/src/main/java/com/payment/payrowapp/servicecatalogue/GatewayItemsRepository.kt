package com.payment.payrowapp.servicecatalogue

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.ServicesResponse
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object GatewayItemsRepository {

    val addItemLiveData = MutableLiveData<ServicesResponse>()

    fun getAddItemData(
        context: Context
    ): MutableLiveData<ServicesResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/mobileapis/filterData/filter/" + sharedPreferenceUtil.getGatewayMerchantID(),
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/mobileapis/filterData/filter/" + sharedPreferenceUtil.getGatewayMerchantID(),
                "{}"
            ),
            context
        )
        ApiClient.apiService.getGatewayItems(headerMap, sharedPreferenceUtil.getGatewayMerchantID())
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
                        Log.v("decrypt",decrypt)
                        val servicesResponse = Gson().fromJson(
                            decrypt.toString(),
                            ServicesResponse::class.java
                        )
                        addItemLiveData.postValue(servicesResponse)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error) + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        //Log.i("Response", "resp->" + Gson().toJson(response))
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })

        return addItemLiveData
    }

    fun getLiveData(): MutableLiveData<ServicesResponse> {
        return addItemLiveData
    }
}