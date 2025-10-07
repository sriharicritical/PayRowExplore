package com.payment.payrowapp.qrcodescan

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.QRCodeResponse
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.dataclass.BarCodeResponse

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object QRCodeScanActivityRepository {
    val mutableLiveData = MutableLiveData<QRCodeResponse>()

    fun getMutableLiveData(
        context: Context,
        invoiceNumber: String
    ): MutableLiveData<QRCodeResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/mobileapis/payrow/getQrCodeOrderDetails/$invoiceNumber",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/mobileapis/payrow/getQrCodeOrderDetails/$invoiceNumber",
                "{}"
            ),
            context
        )
        ApiClient.apiSoftPOS.getQRCodeInvoice(
            headerMap,
            invoiceNumber
        ).enqueue(object : Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                if (response.code() == 200 && response.isSuccessful) {
                    try {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        val qrCodeResponse = Gson().fromJson(
                            decrypt.toString(),
                            QRCodeResponse::class.java
                        )
                        mutableLiveData.postValue(qrCodeResponse)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.data_not_available) ,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.failure) +context.getString(R.string.service_unavialable), Toast.LENGTH_LONG).show()
            }
        })

        return mutableLiveData
    }

    fun getLiveData(): MutableLiveData<QRCodeResponse> {
        return mutableLiveData
    }

    private val barMutableLiveData = MutableLiveData<BarCodeResponse>()

    fun getBarCodeMutableLiveData(
        context: Context,
        invoiceNumber: String
    ): MutableLiveData<BarCodeResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/mobileapis/getPosServbyId/$invoiceNumber",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/mobileapis/getPosServbyId/$invoiceNumber",
                "{}"
            ),
            context
        )
        ApiClient.apiSoftPOS.getBarCodeItem(
            headerMap,
            invoiceNumber
        ).enqueue(object : Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                if (response.code() == 200 && response.isSuccessful) {
                    try {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                       /* val barCodeResponse = Gson().fromJson(
                            decrypt.toString(),
                            BarCodeResponse::class.java)*/
                        val dataObject = JSONObject(decrypt.toString())
                        val barCodeResponse = BarCodeResponse(dataObject.getBoolean("success"),
                        dataObject.getString("data"),dataObject.getString("status"))

                        barMutableLiveData.postValue(barCodeResponse)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.data_not_available) ,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.failure) +context.getString(R.string.service_unavialable), Toast.LENGTH_LONG).show()
            }
        })

        return barMutableLiveData
    }

    fun getBarLiveData(): MutableLiveData<BarCodeResponse> {
        return barMutableLiveData
    }



    private val gatewayBarMutableLiveData = MutableLiveData<BarCodeResponse>()

    fun getGatewayBarCodeMutableLiveData(
        context: Context,
        invoiceNumber: String
    ): MutableLiveData<BarCodeResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/mobileapis/getGWServbyId/$invoiceNumber",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/mobileapis/getGWServbyId/$invoiceNumber",
                "{}"
            ),
            context
        )
        ApiClient.apiSoftPOS.getGatewayBarCodeItem(
            headerMap,
            invoiceNumber
        ).enqueue(object : Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                if (response.code() == 200 && response.isSuccessful) {
                    try {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        /* val barCodeResponse = Gson().fromJson(
                             decrypt.toString(),
                             BarCodeResponse::class.java)*/
                        val dataObject = JSONObject(decrypt.toString())
                        val barCodeResponse = BarCodeResponse(dataObject.getBoolean("success"),
                            dataObject.getString("data"),dataObject.getString("status"))

                        gatewayBarMutableLiveData.postValue(barCodeResponse)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.data_not_available) ,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.failure) +context.getString(R.string.service_unavialable), Toast.LENGTH_LONG).show()
            }
        })

        return gatewayBarMutableLiveData
    }

    fun getGatewayBarLiveData(): MutableLiveData<BarCodeResponse> {
        return gatewayBarMutableLiveData
    }
}