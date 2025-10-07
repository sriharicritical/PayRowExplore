package com.payment.payrowapp.invoicerecall

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

object SendReceiptRepository {

    val pdfReceiptLiveData = MutableLiveData<PDFReceiptResponse>()
    val sendURLLiveData = MutableLiveData<SendURLResponse>()
    val tidReportLiveData = MutableLiveData<TIDReportResp>()

    fun getPDFReceiptData(
        context: Context,
        invoiceNumber: String
    ): MutableLiveData<PDFReceiptResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/invoice/generate/invoice/$invoiceNumber",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/invoice/generate/invoice/$invoiceNumber",
                "{}"
            ),
            context
        )
        ApiClient.apiSoftPOS.getPDFReceipt(
            headerMap,
            invoiceNumber
        ).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                Log.d("Request", "req->" + call.request().body.toString())
                if (response.code() == 200 && response.isSuccessful) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )

                    val pdfReceiptResponse = Gson().fromJson(
                        decrypt.toString(),
                        PDFReceiptResponse::class.java
                    )
                    pdfReceiptLiveData.postValue(pdfReceiptResponse)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error),
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
                ).show()
            }
        })

        return pdfReceiptLiveData
    }

    fun getLiveData(): MutableLiveData<PDFReceiptResponse> {
        return pdfReceiptLiveData
    }


    fun sendURLTOMail(
        context: Context,
        sendURLRequest: SendURLRequest
    ): MutableLiveData<SendURLResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(sendURLRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/mobileapis/payrow/sendUrl", timeStamp, context)

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
                "/mobileapis/payrow/sendUrl",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.sendURL(
            headerMap,
            encryptDataClass
        ).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                Log.d("Request", "req->" + call.request().body.toString())
                if (response.code() == 200 && response.isSuccessful) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )
                    val sendURLResponse = Gson().fromJson(
                        decrypt.toString(),
                        SendURLResponse::class.java
                    )
                    sendURLLiveData.postValue(sendURLResponse)
                    Toast.makeText(
                        context,
                        context.getString(R.string.email_sent),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.email_sending_failed),
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
                ).show()
            }
        })

        return sendURLLiveData
    }

    fun sendURLLiveData(): MutableLiveData<SendURLResponse> {
        return sendURLLiveData
    }

    fun sendTIDReport(
        context: Context,
        sendTIDReportReq: SendTIDReportReq
    ): MutableLiveData<TIDReportResp> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(sendTIDReportReq)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/sendTidreport",
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
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "POST",
                "/mobileapis/payrow/sendTidreport",
                jsonString2
            ),
            context
        )

        ApiClient.apiSoftPOS.sendTIDReport(
            headerMap,
            encryptDataClass
        ).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                Log.d("Request", "req->" + call.request().body.toString())
                if (response.code() == 200 && response.isSuccessful) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )
                    val tidReportReq = Gson().fromJson(
                        decrypt.toString(),
                        TIDReportResp::class.java
                    )
                    when (tidReportReq.status) {
                        200 -> {
                            tidReportLiveData.postValue(tidReportReq)
                            Toast.makeText(
                                context,
                                context.getString(R.string.email_sent),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        400 -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_records_found),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error) + context.getString(R.string.no_records_found),
                                Toast.LENGTH_LONG
                            ).show()
                            //Log.i("Response", "resp->" + Gson().toJson(response))
                        }
                    }

                } else if (response.code() == 400) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_records_found),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error) + context.getString(R.string.no_records_found),
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
                ).show()
            }
        })

        return tidReportLiveData
    }

}