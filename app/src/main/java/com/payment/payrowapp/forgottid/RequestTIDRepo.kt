package com.payment.payrowapp.forgottid

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.ReqForTIDResp
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RequestTIDRepo {

    val mutableLiveData = MutableLiveData<ReqForTIDResp>()

    fun getMutableLiveData(
        context: Context,
        email: String, mobileNumber:String,sharedPreferenceUtil: SharedPreferenceUtil
    ): MutableLiveData<ReqForTIDResp> {

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey("GET",
            "/onboarding/login/find/$email/$mobileNumber",timeStamp, context)

        val headerMap = ContextUtils.getHeaderMap("",
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,"GET",
                "/onboarding/login/find/$email/$mobileNumber","{}"),
            context
        )
        ApiClient.apiServiceKYC.requestTID(headerMap,email,mobileNumber)
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
                        val reqForTIDResp = Gson().fromJson(
                            decrypt.toString(),
                            ReqForTIDResp::class.java
                        )
                        mutableLiveData.postValue(reqForTIDResp)
                    } else if (response.code() == 300) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.terminal_id_not_available),
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (response.code() == 400) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.merchant_not_found),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    Toast.makeText(context, context.getString(R.string.failure) +context.getString(R.string.service_unavialable), Toast.LENGTH_LONG)
                        .show()
                }
            })

        return mutableLiveData
    }

    fun getLiveData(): MutableLiveData<ReqForTIDResp> {
        return mutableLiveData
    }
}