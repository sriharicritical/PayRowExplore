package com.payment.payrowapp.generateqrcode

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

object GenarateQRCodeRepository {
    val mutableLiveData = MutableLiveData<QRResponse>()

    fun getMutableLiveData(
        context: Context,
        generateQRRequest: GenerateQRRequest
    ): MutableLiveData<QRResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(generateQRRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/payrowGenerateQR",
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
                "/mobileapis/payrow/payrowGenerateQR",
                jsonString2
            ),
            context
        )
        ApiClient.apiServiceGateway.generateQRCode(headerMap, encryptDataClass).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                if (response.code() == 200 && response.isSuccessful) {

                    if (response.body()?.data != null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.qr_generated),
                            Toast.LENGTH_LONG
                        ).show()
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()?.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val qrResponse = Gson().fromJson(
                            decrypt.toString(),
                            QRResponse::class.java
                        )
                        mutableLiveData.postValue(qrResponse)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.qrcode_filed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.qrcode_filed),
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

    fun getLiveData(): MutableLiveData<QRResponse> {
        return mutableLiveData
    }

    val enquiryMutableLiveData = MutableLiveData<EnquiryResponseDataClass>()

    fun getEnquiryMutableLiveData(
        context: Context,
        enquiryRequestClass: EnquiryRequestClass, onResult: ((Boolean) -> Unit)
    ): MutableLiveData<EnquiryResponseDataClass> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(enquiryRequestClass)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/inquiryDetails",
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
                "/mobileapis/payrow/inquiryDetails",
                jsonString2
            ),
            context
        )
        ApiClient.apiServiceGateway.gateWayEnquiry(headerMap, encryptDataClass).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                if (response.code() == 200 && response.isSuccessful && response.body()?.data != null) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()?.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )
                    if (decrypt != null) {
                        val enquiryResponseDataClass = Gson().fromJson(
                            decrypt.toString(),
                            EnquiryResponseDataClass::class.java
                        )
                        enquiryMutableLiveData.postValue(enquiryResponseDataClass)
                    }
                } else {
                    onResult(true)
                    /*   Toast.makeText(
                           context,
                           context.getString(R.string.data_not_available)+context.getString(R.string.please_try_again),
                           Toast.LENGTH_LONG
                       ).show()*/
                }
            }

            override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                onResult(true)
                /*  Toast.makeText(
                      context,
                      context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                      Toast.LENGTH_LONG
                  ).show()*/
            }
        })

        return enquiryMutableLiveData
    }

    fun getEnquiryLiveData(): MutableLiveData<EnquiryResponseDataClass> {
        return enquiryMutableLiveData
    }
}