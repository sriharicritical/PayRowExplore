package com.payment.payrowapp.login

import android.content.Context
import android.content.Intent
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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object CreatePINRepository {
    val mutableLiveData = MutableLiveData<DecryptResponse>()

    fun getMutableLiveData(
        context: Context,
        createPINRequest: CreatePINRequest,
        sharedPreferenceUtil: SharedPreferenceUtil,
        loginActivity: LoginActivity
    ): MutableLiveData<DecryptResponse> {

        val jsonObject = JSONObject()
        jsonObject.put("terminalId", createPINRequest.terminalId)
        jsonObject.put("pin", createPINRequest.pin)
        jsonObject.put("status", createPINRequest.status)
        jsonObject.put("keyNum", ContextUtils.randomValue())

        /* val encrypt = EncryptDecrypt.encrypt(
             jsonObject.toString(),
             sharedPreferenceUtil.getAESKey(),
             sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlg()
         )*/
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/onboarding/login/pin", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonObject.toString(),
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )
        //   val encryptDataClass = EncryptDataClass(encryptData)
        val verifyOTPRequestClass = VerifyOTPRequestClass(encryptData)
        val jsonString2 = Gson().toJson(verifyOTPRequestClass)

        val headerMap = ContextUtils.getHeaderMap(
            "",
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/onboarding/login/pin",
                jsonString2
            ),
            context
        )

        ApiClient.apiServiceKYC.createPIN(headerMap, verifyOTPRequestClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        mutableLiveData.postValue(response.body())
                        Toast.makeText(
                            context,
                            context.getString(R.string.create_pin),
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (response.code() == 400) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.key_expired),
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (response.code() == 404) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.pin_already_used),
                            Toast.LENGTH_LONG
                        ).show()
                        val intent =
                            Intent(
                                context,
                                CreatePinActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        loginActivity.startActivity(intent)
                        loginActivity.finish()
                    } else {
                        Toast.makeText(
                            context,
                            response.message().toString(),
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

    fun getLiveData(): MutableLiveData<DecryptResponse> {
        return mutableLiveData
    }
}