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
import com.payment.payrowapp.utils.LoaderCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object EnterTransactionIdRepository {
    val mutableLiveData = MutableLiveData<QRCodeResponse>()
    val dailyReportData = MutableLiveData<DailyReportResponse>()

    fun getMutableLiveData(
        context: Context,
        paymentInvoiceRequest: PaymentInvoiceRequest,loaderCallback: LoaderCallback
    ): MutableLiveData<QRCodeResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(paymentInvoiceRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("PUT", "/api/orders/payInvoice", timeStamp, context)

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
                "PUT",
                "/api/orders/payInvoice",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.getPaymentInvoice(headerMap, encryptDataClass).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                Log.d("Request", "req->" + call.request().body.toString())
                if (response.code() == 200 && response.isSuccessful &&response.body()?.data!=null) {

                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()?.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )

                    val qrCodeResponse = Gson().fromJson(
                        decrypt.toString(),
                        QRCodeResponse::class.java
                    )
                    mutableLiveData.postValue(qrCodeResponse)

                } else {
                    loaderCallback.closeLoader()
                    Toast.makeText(
                        context,
                        context.getString(R.string.error) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    ).show()
                    //  Log.i("Response", "resp->" + Gson().toJson(response))
                }
            }

            override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                loaderCallback.closeLoader()
                Toast.makeText(
                    context,
                    context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        return mutableLiveData
    }

    fun getDailyReportMutableLiveData(
        context: Context,
        invoiceRecallByDatesRequest: InvoiceRecallByDatesRequest, loaderCallback: LoaderCallback
    ): MutableLiveData<DailyReportResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(invoiceRecallByDatesRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/paymentdetails",
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
                "/mobileapis/payrow/paymentdetails",
                jsonString2
            ),
            context
        )
        ApiClient.apiService.getInvoiceReport(
            headerMap,
            encryptDataClass
        )
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.body()?.data!=null) {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        val dailyReportResponse = Gson().fromJson(
                            decrypt.toString(),
                            DailyReportResponse::class.java
                        )
                        if (response.code() == 200 && response.isSuccessful) {
                            dailyReportData.postValue(dailyReportResponse)
                        } else {
                            loaderCallback.closeLoader()
                        // dailyReportData.postValue(dailyReportResponse)
                            /*Toast.makeText(
                            context,
                            "Error: " + "No records found",
                            Toast.LENGTH_LONG
                        ).show()*/
                        }
                    } else {
                        loaderCallback.closeLoader()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    loaderCallback.closeLoader()
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        return dailyReportData
    }


    fun getLiveData(): MutableLiveData<QRCodeResponse> {
        return mutableLiveData
    }

    fun getDailyReportLiveData(): MutableLiveData<DailyReportResponse> {
        return dailyReportData
    }
}