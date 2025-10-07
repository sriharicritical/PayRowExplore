package com.payment.payrowapp.introduction

import android.content.Context
import android.content.Intent
import android.util.Base64
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
import com.payment.payrowapp.sunmipay.ByteUtil
import com.payment.payrowapp.sunmipay.Constant
import com.payment.payrowapp.sunmipay.EmvUtil
import com.payment.payrowapp.sunmipay.LogUtil
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets

object IntroductionRepo {

    val mutableLiveData = MutableLiveData<DecryptResponse>()

    val mutableLiveData2 = MutableLiveData<CertResp>()

    fun getMutableLiveData(
        context: Context
    ): MutableLiveData<DecryptResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/onboarding/login/imeiCheck/" + ContextUtils.getDeviceId(context),
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "",
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/onboarding/login/imeiCheck/" + ContextUtils.getDeviceId(context),
                "{}"
            ),
            context
        )
        ApiClient.apiServiceKYC.imeiCheck(headerMap, ContextUtils.getDeviceId(context))
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        val decrpt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        val jsonObject1 = JSONObject(decrpt)
                        val key = jsonObject1.getString("data")
                        val decodedString: ByteArray =
                            Base64.decode(key, Base64.DEFAULT)
                        val text = String(decodedString, StandardCharsets.UTF_8)

                        val jsonObject = JSONObject(text)
                      //  sharedPreferenceUtil.setAESKey(jsonObject.getString("key"))
                    //    sharedPreferenceUtil.setIV(jsonObject.getString("iv"))
                     //   sharedPreferenceUtil.setAlgorithm(jsonObject.getString("AES"))
                      //  sharedPreferenceUtil.setAlg(jsonObject.getString("ALG"))
                        mutableLiveData.postValue(response.body())
                    } else if (response.code() == 400) {

                        try {
                            sharedPreferenceUtil.clearPreferences()
                        } catch (e: IllegalArgumentException) {

                        }

                        Toast.makeText(
                            context,
                            context.getString(R.string.device_notregister),
                            Toast.LENGTH_LONG
                        ).show()
                        sharedPreferenceUtil.setISLogin(false)
                        /* editor.putBoolean(Constants.IS_LOGIN, false)
                         editor.apply()*/

                        val intent = Intent(context, EnterTIDActivity::class.java)
                        context.startActivity(intent)
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

    val mutableLiveData3 = MutableLiveData<KSNResponseClass>()


}