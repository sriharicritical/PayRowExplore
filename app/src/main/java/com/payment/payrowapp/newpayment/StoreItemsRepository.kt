package com.payment.payrowapp.newpayment

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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object StoreItemsRepository {

    val addItemLiveData = MutableLiveData<ServicesResponse>()

    fun getAddItemData(
        context: Context
    ): MutableLiveData<ServicesResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/mobileapis/posServices/" + sharedPreferenceUtil.getMerchantID(),
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/mobileapis/posServices/" + sharedPreferenceUtil.getMerchantID(),
                "{}"
            ),
            context
        )
        ApiClient.apiService.getAddItems(headerMap, sharedPreferenceUtil.getMerchantID())
            .enqueue(object :
                Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    Log.d("Request", "req->" + call.request().body.toString())
                    if (response.isSuccessful) {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                      Log.v("decrypt",decrypt)
                        val servicesResponse = Gson().fromJson(
                            decrypt.toString(),
                            ServicesResponse::class.java
                        )
                        addItemLiveData.postValue(servicesResponse)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error) + response.body()?.toString(),
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
                    )
                        .show()
                }
            })

        return addItemLiveData
    }

    fun getLiveData(): MutableLiveData<ServicesResponse> {
        return addItemLiveData
    }

    val itemCountMutableLiveData = MutableLiveData<BarCodeResponse>()

    fun getItemCountMutableLiveData(
        context: Context, itemsCountRequest: ItemsCountRequest, sharedPreferenceUtil: SharedPreferenceUtil,
        onResult: ((Boolean) -> Unit)? = null
    ): MutableLiveData<BarCodeResponse> {

        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(itemsCountRequest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/mobileapis/getDefServ", timeStamp, context)

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
                "/mobileapis/getDefServ",
                jsonString2
            ),
            context
        )

        ApiClient.apiServiceKYC.getPosGateWayItemCount(headerMap,encryptDataClass)
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

                        if (decrypt!=null) {
                        val dataObject = JSONObject(decrypt.toString())
                        val barCodeResponse = BarCodeResponse(dataObject.getBoolean("success"),
                            dataObject.getString("data"),dataObject.getString("status"))
                        itemCountMutableLiveData.postValue(barCodeResponse)
                        } else {
                            onResult?.invoke(true)
                        }
                    } else {
                        onResult?.invoke(true)
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    onResult?.invoke(true)
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })

        return itemCountMutableLiveData
    }


    fun getItemCountLiveData(): MutableLiveData<BarCodeResponse> {
        return itemCountMutableLiveData
    }
}