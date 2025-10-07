package com.payment.payrowapp.contactpayrow

import android.content.Context
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

object SupportRepository {
    val mutableLiveData = MutableLiveData<DecryptResponse>()

    fun getMutableLiveData(
        context: Context,
        complaintRequest: ComplaintRequest
    ): MutableLiveData<DecryptResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(complaintRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/api/complaints", timeStamp, context)

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
                "/api/complaints",
                jsonString2
            ),
            context
        )

        ApiClient.apiSoftPOS.postComplaint(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        mutableLiveData.postValue(response.body())
                        // context.startActivity(Intent(context, DashboardActivity::class.java))
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error) + response.message().toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        return mutableLiveData
    }

    fun getLiveData(): MutableLiveData<DecryptResponse> {
        return mutableLiveData
    }
}