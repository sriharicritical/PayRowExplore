package com.payment.payrowapp.paymenthistory

import android.content.Context
import android.util.Base64
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


object PaymentHistoryRepository {
    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    val totalAmountData = MutableLiveData<TotalAmountResponse>()
    val totalSalesData = MutableLiveData<TotalSalesResponse>()
    val totalReportData = MutableLiveData<TotalReportResponse>()
    var dailyReportData = MutableLiveData<DailyReportResponse>()
    var monthlyReportData = MutableLiveData<MonthlyReportResponse>()

    fun getTotalAmountMutableLiveData(
        context: Context,
        terminalId: String
    ): MutableLiveData<TotalAmountResponse> {
        sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/api/orders/totalAmount/$terminalId",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil?.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "GET",
                "/api/orders/totalAmount/$terminalId",
                "{}"
            ),
            context
        )

        ApiClient.apiService.getTotalAmount(
            headerMap,
            terminalId
        ).enqueue(object : Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                if (response.code() == 200 && response.isSuccessful) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil?.getIV(), ApiKeys.AES_Algorithm
                    )

                    val totalAmountResponse = Gson().fromJson(
                        decrypt.toString(),
                        TotalAmountResponse::class.java
                    )
                    totalAmountData.postValue(totalAmountResponse)
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

        return totalAmountData
    }

    fun getTotalSalesMutableLiveData(
        context: Context,
        userId: String
    ): MutableLiveData<TotalSalesResponse> {

        sharedPreferenceUtil = SharedPreferenceUtil(context)

        val jsonObject = JSONObject()
        jsonObject.put("merchantId", sharedPreferenceUtil!!.getMerchantID())
        jsonObject.put("tid", sharedPreferenceUtil!!.getTerminalID())
        jsonObject.put("imeiNumber", ContextUtils.getDeviceId(context))

        val timeStamp = HeaderSignatureUtil.getISO()

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/api/orders/sales", timeStamp, context)

        // encrypt data
        val encryptData = EncryptDecrypt.encrypt(
            jsonObject.toString(),
            secretKey,
            sharedPreferenceUtil?.getIV(),
            ApiKeys.AES_Algorithm
        )

        val encryptDataClass = EncryptDataClass(encryptData)
        val jsonString2 = Gson().toJson(encryptDataClass)

        //generate headers
        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil?.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/api/orders/sales",
                jsonString2
            ),
            context
        )

        ApiClient.apiService.getTotalSales(
            headerMap,
            encryptDataClass
        )
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil?.getIV(), ApiKeys.AES_Algorithm
                        )
                        val totalSalesResponse = Gson().fromJson(
                            decrypt.toString(),
                            TotalSalesResponse::class.java
                        )
                        totalSalesData.postValue(totalSalesResponse)
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
                    )
                        .show()
                }
            })

        return totalSalesData
    }

    fun getTotalReportMutableLiveData(
        context: Context,
        userId: String
    ): MutableLiveData<TotalReportResponse> {
        sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/api/orders/totalReport/$userId",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil?.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "GET",
                "/api/orders/totalReport/$userId",
                "{}"
            ),
            context
        )

        ApiClient.apiService.getTotalReport(
            headerMap,
            userId
        )
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil?.getIV(), ApiKeys.AES_Algorithm
                        )

                        val totalReportResponse = Gson().fromJson(
                            decrypt.toString(),
                            TotalReportResponse::class.java
                        )
                        totalReportData.postValue(totalReportResponse)
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
                    )
                        .show()
                }
            })

        return totalReportData
    }

    fun getDailyReportMutableLiveData(
        context: Context,
        dailyReportRequest: DailyReportRequest
    ): MutableLiveData<DailyReportResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(dailyReportRequest)

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
        dailyReportData = MutableLiveData<DailyReportResponse>()
        ApiClient.apiService.getDailyReport(
            headerMap,
            encryptDataClass
        )
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )
                    val dailyReportResponse = Gson().fromJson(
                        decrypt.toString(),
                        DailyReportResponse::class.java
                    )
                    //  dailyReportData.postValue(dailyReportResponse)
                    if (response.code() == 200 && response.isSuccessful) {

                        dailyReportData.postValue(dailyReportResponse)
                    } else {
                        dailyReportData.postValue(dailyReportResponse)
                        /*Toast.makeText(
                            context,
                            "Error: " + "No records found",
                            Toast.LENGTH_LONG
                        ).show()*/
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

        return dailyReportData
    }

    fun getMonthlyReportMutableLiveData(
        context: Context,
        year: Int, channel: String
    ): MutableLiveData<MonthlyReportResponse> {
        val jsonObject = JSONObject()
        jsonObject.put("num", ContextUtils.randomValue())
        jsonObject.put("validation", "Key Validation")
        val encodedString =
            Base64.encodeToString(jsonObject.toString().toByteArray(), Base64.DEFAULT)
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val monthlyReportRequest =
            MonthlyReportRequest(
                year, channel, sharedPreferenceUtil.getMerchantID(), encodedString,
                sharedPreferenceUtil.getTerminalID()
            )
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(monthlyReportRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/mobileapis/payrow/monthly", timeStamp, context)

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
                "/mobileapis/payrow/monthly",
                jsonString2
            ),
            context
        )
        monthlyReportData = MutableLiveData<MonthlyReportResponse>()
        ApiClient.apiService.getMonthlyReport(
            headerMap,
            encryptDataClass
        )
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )
                    val monthlyReportRequest = Gson().fromJson(
                        decrypt.toString(),
                        MonthlyReportResponse::class.java
                    )
                    if (response.code() == 200 && response.isSuccessful) {
                        monthlyReportData.postValue(monthlyReportRequest)
                    } else {
                        monthlyReportData.postValue(monthlyReportRequest)
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

        return monthlyReportData
    }

    fun getTotalAmountLiveData(): MutableLiveData<TotalAmountResponse> {
        return totalAmountData
    }

    fun getTotalSalesLiveData(): MutableLiveData<TotalSalesResponse> {
        return totalSalesData
    }

    fun getTotalReportLiveData(): MutableLiveData<TotalReportResponse> {
        return totalReportData
    }

    fun getDailyReportLiveData(): MutableLiveData<DailyReportResponse> {
        return dailyReportData
    }

    fun getMonthlyReportLiveData(): MutableLiveData<MonthlyReportResponse> {
        return monthlyReportData
    }
}