package com.payment.payrowapp.paymenthistory

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.EncryptDataClass
import com.payment.payrowapp.dataclass.SummaryReportRequest
import com.payment.payrowapp.dataclass.SummaryReportResp
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SummaryReportRepository {

    val mutableLiveData = MutableLiveData<SummaryReportResp>()

    fun getMutableLiveData(
        context: Context, summaryReportRequest: SummaryReportRequest, sharedPreferenceUtil: SharedPreferenceUtil
    ): MutableLiveData<SummaryReportResp> {

        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(summaryReportRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/mobileapis/payrow/getTransactionSummary", timeStamp, context)

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
                "/mobileapis/payrow/getTransactionSummary",
                jsonString2
            ),
            context
        )

        ApiClient.apiServiceKYC.getTransSummary(headerMap,encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful && response.body()?.data!=null) {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()?.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val summaryReportResp = Gson().fromJson(
                            decrypt.toString(),
                            SummaryReportResp::class.java
                        )
                        mutableLiveData.postValue(summaryReportResp)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed_please_try_again),
                            Toast.LENGTH_LONG
                        ).show()
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

        return mutableLiveData
    }


    fun getLiveData(): MutableLiveData<SummaryReportResp> {
        return mutableLiveData
    }
}