package com.payment.payrowapp.cashinvoice

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

object EnterAmountToPayCashRepository {
    val orderResponseLiveData = MutableLiveData<OrderResponse>()
    val postQRResponseLiveData = MutableLiveData<String>()

    fun getOrderMutableLiveData(
        context: Context,
        orderRequest: OrderRequest
    ): MutableLiveData<OrderResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(orderRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/api/orders", timeStamp, context)

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
                "/api/orders",
                jsonString2
            ),
            context
        )
        ApiClient.apiService.addCashOrder(headerMap, encryptDataClass)
            .enqueue(object :
                Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    Log.d("Request", "req->" + call.request().body.toString())
                    if (response.isSuccessful && response.code() == 200) {

                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val orderResponse = Gson().fromJson(
                            decrypt.toString(),
                            OrderResponse::class.java
                        )
                        orderResponseLiveData.postValue(orderResponse)
//                    val merchantId=MerchantId("9876")
//                    EventBus.getDefault().post(merchantId)

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

        return orderResponseLiveData
    }

    fun postQRResponseMutableLiveData(
        context: Context,
        jsonObject: OrderResponse,
        url: String
    ) {

        /* val index = url.lastIndexOf("/")
         val retrofit = Retrofit.Builder()
             .baseUrl(url.substring(0, index + 1))
             .addConverterFactory(GsonConverterFactory.create())
             .client(ApiClient.getMyHttpClient().build())
             .build()

         val qrCallBackData = prepareQRCallBackData(jsonObject.data)

         if (qrCallBackData.checkoutStatus=="CAPTURED") {
             qrCallBackData.errorMessage = "Successful operation."
         } else {
             qrCallBackData.errorMessage = "Failed operation."
         }

         // qrResponseCalback.payload.amount = qrResponseCalback.payload.totalAmount
         if (qrCallBackData.checkoutStatus == "Cancelled" || qrCallBackData.checkoutStatus == "Terminal Declined") {
             qrCallBackData.checkoutStatus = "NOT CAPTURED"
             qrCallBackData.orderStatus = "NOT CAPTURED"
         } else if (qrCallBackData.checkoutStatus == "Closed") {
             qrCallBackData.checkoutStatus = "Cancelled"
             qrCallBackData.orderStatus = "NOT CAPTURED"
         }
           val jsonString = Gson().toJson(qrCallBackData)*/
        // val qrResponseCallback = QRResponseCalback(qrCallBackData)

        // val payLoadObject = JSONObject()
        // payLoadObject.put("payload", qrResponseCallback)


        ApiClient.apiService.postBackURL(jsonObject.data.orderNumber)
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error) + response.message().toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    /* Toast.makeText(
                         context,
                         context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                         Toast.LENGTH_LONG
                     )
                         .show()*/
                }
            })
        /*val apiService = retrofit.create(ApiService::class.java)
        apiService.postQRResponse(url.substring(index + 1, url.length), jsonString.toString())
            .enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d("Request", "req->" + call.request().body.toString())
                    if (response.isSuccessful) {

                        postQRResponseLiveData.postValue(response.body())
                    } else {

                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })*/

        //   return postQRResponseLiveData
    }

    private fun prepareQRCallBackData(data: OrderData): QRCallBackOrderData {
        return QRCallBackOrderData(
            data.purchaseBreakdown,
            data.channel,
            data.paymentDate,
            data.totalTaxAmount.toFloat(),
            data.totalAmount.toFloat(),
            data.mainMerchantId,
            data.checkoutStatus,
            data.checkoutStatus,
            data.orderNumber,
            data._id,
            data.customerName,
            data.customerEmail,
            data.customerPhone,
            data.customerBillingCity,
            data.customerBillingState,
            data.customerBillingCountry,
            data.customerBillingPostalCode,
            data.customerAddressLine1,
            data.customerAddressLine2,
            data.checkoutId,
            data.totalAmount.toFloat(),
            data.PartialApprovedAmount,
            data.merchantBankTransferReturnUrl,
            data.cardType,
            data.cardNumber,
            "",
            data.authorizationId,
            data.checkoutUrl
        )
    }

    fun getOrderLiveData(): MutableLiveData<OrderResponse> {
        return orderResponseLiveData
    }

}