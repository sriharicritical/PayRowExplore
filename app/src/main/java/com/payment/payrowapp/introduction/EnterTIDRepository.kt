package com.payment.payrowapp.introduction

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.dialogs.LogoutDialog
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


object EnterTIDRepository {
    val mutableLiveData = MutableLiveData<VerifyDeviceResponse>()

    fun getMutableLiveData(
        context: Context,
        verifyDeviceRequest: VerifyDeviceRequest, sharedPreferenceUtil: SharedPreferenceUtil
    ): MutableLiveData<VerifyDeviceResponse> {

        val jsonString = Gson().toJson(verifyDeviceRequest)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/onboarding/login/checkDevice",
                timeStamp,
                context
            )

        val encryptData = EncryptDecrypt.encrypt(
            jsonString,
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )
        val testDataClass = EncryptDataClass(encryptData)

        val jsonString2 = Gson().toJson(testDataClass)

        val headerMap = ContextUtils.getHeaderMap(
            "",
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/onboarding/login/checkDevice",
                jsonString2
            ),
            context
        )
        ApiClient.apiServiceKYC.verifyDevice(headerMap, testDataClass)
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

                        val verifyDeviceResponse = Gson().fromJson(
                            decrpt,
                            VerifyDeviceResponse::class.java
                        )
                        //  Log.v("decrpt", decrpt)
                        mutableLiveData.postValue(verifyDeviceResponse)
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_enter_authcode),
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (response.code() == 500) {
                        val errorBody = response.errorBody()?.string()
                        val decryptResponse = Gson().fromJson(
                            errorBody.toString(),
                            DecryptResponse::class.java
                        )
                        //  mutableLiveData.postValue(response.body())
                        val decrpt = EncryptDecrypt.decrypt(
                            decryptResponse.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        val jsonObject = JSONObject(decrpt)
                        val deviceLogoutRequest = DeviceLogoutRequest(
                            verifyDeviceRequest.tid, ContextUtils.getDeviceId(context)
                        )
                        LogoutDialog(
                            context,
                            deviceLogoutRequest
                        ).show()
                        //   getLogoutMutableLiveData(context, deviceLogoutRequest)
                    } else if (response.code() == 400) {
                        //  mutableLiveData.postValue(response.body())
                        Toast.makeText(
                            context,
                            context.getString(R.string.device_not_found) + verifyDeviceRequest.tid,
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (response.code() == 300) {
                        // mutableLiveData.postValue(response.body())
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed_send_authcode),
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


    fun getLiveData(): MutableLiveData<VerifyDeviceResponse> {
        return mutableLiveData
    }

    fun getKeyMutableLiveData(
        context: Context, sharedPreferenceUtil: SharedPreferenceUtil
    ): MutableLiveData<VerifyDeviceResponse> {

        val UUID = ContextUtils.generateUUID()
        val randomValue = ContextUtils.randomValue().toString()
        val jsonObject = JSONObject()
        jsonObject.put("deviceId", UUID)
        jsonObject.put("keyValidation", randomValue)

        val byteArrayKey = EncryptDecrypt.generateKey(UUID, randomValue)
        Log.v("secretKey", "")

        //To hex conversion
        val data = jsonObject.toString().toByteArray(StandardCharsets.UTF_8)
        val hexStr = EncryptDecrypt.byteArrayToHexString(data)

        Log.v("decoded value", hexStr)

        val initKey = InitKey(hexStr)

        ApiClient.apiServiceKYC.initKey(initKey)
            .enqueue(object : Callback<InitResponse> {
                override fun onResponse(
                    call: Call<InitResponse>,
                    response: Response<InitResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        try {
                            val decrypt = EncryptDecrypt.decryptDataGCM(
                                response.body()!!.data.encryptedData,
                                response.body()!!.data.validation,
                                response.body()!!.data.authTag,
                                byteArrayKey
                            )

                            Log.v("Decrypt", decrypt)

                            val jsonObject = JSONObject(decrypt)
                            val key = jsonObject.getString("keyValidation")
                            if (randomValue == key) {
                                sharedPreferenceUtil.setIV(jsonObject.getString("iv"))
                                sharedPreferenceUtil.setSecretKey(jsonObject.getString("Secret_Key"))
                                sharedPreferenceUtil.setSignatureKey(jsonObject.getString("SIGNATURE_KEY"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<InitResponse>, t: Throwable) {
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


    fun getInitKeyLiveData(): MutableLiveData<DecryptResponse> {
        return initMutableLiveData
    }

    val initMutableLiveData = MutableLiveData<DecryptResponse>()

}