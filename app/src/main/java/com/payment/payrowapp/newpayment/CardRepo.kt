package com.payment.payrowapp.newpayment

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.TerminalResponse
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CardRepo {
    val terminalLiveData = MutableLiveData<DecryptResponse>()

    fun getTerminalData(
        context: Context
    ): MutableLiveData<DecryptResponse> {


        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/api/terminaldata/terminaldata",
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/api/terminaldata/terminaldata",
                "{}"
            ),
            context
        )
        ApiClient.apiSoftPOS.getTerminalData(
            headerMap
        ).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                Log.d("Request", "req->" + call.request().body.toString())
                if (response.isSuccessful) {
                    terminalLiveData.postValue(response.body())

                    val decrypt = EncryptDecrypt.decrypt(
                        response.body()!!.data,
                        secretKey,
                        sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                    )

                    val terminalResponse = Gson().fromJson(
                        decrypt.toString(),
                        TerminalResponse::class.java
                    )
                    val data = terminalResponse?.data
                    sharedPreferenceUtil.setAccInstIdCode(data!![0].ACQUIRER_INST_ID_CODE)
                    sharedPreferenceUtil.setMIT(data[0].MTI)
                    sharedPreferenceUtil.setProcessingCode(data[0].PROCESSING_CODE)
                    sharedPreferenceUtil.setSystemTraceAuditNumber(data[0].SYSTEM_TRACE_AUDIT_NUMBER)

                    sharedPreferenceUtil.setMerchantType(data[0].MERCHANT_TYPE)
                    sharedPreferenceUtil.setAppPanSequenceNo(data[0].APP_PAN_SEQUENCE_NO)
                    sharedPreferenceUtil.setPOSPinCaptureCode(data[0].POS_PIN_CAPTURE_CODE)
                    sharedPreferenceUtil.setPOSConditionCode(data[0].POS_CONDITION_CODE)

                    sharedPreferenceUtil.setCardAcceptorTerminalId(data[0].CARD_ACCEPTOR_TERMINAL_ID)
                    sharedPreferenceUtil.setCardAcceptorIdCode(data[0].CARD_ACCEPTOR_ID_CODE)
                    sharedPreferenceUtil.setCardAcceptorNameLocation(data[0].CARD_ACCEPTOR_NAME_LOCATION)
                    sharedPreferenceUtil.setCurrencyCode(data[0].CURRENCY_CODE)

                    sharedPreferenceUtil.setSponsorBank(data[0].SPONSOR_BANK)
                    sharedPreferenceUtil.setTerminalType(data[0].TERMINAL_TYPE)
                    sharedPreferenceUtil.setPOSEntryMode(data[0].POS_ENTRY_MODE)
                    sharedPreferenceUtil.setPosGeoData(data[0].POS_GEO_DATA)
                    sharedPreferenceUtil.setVoidMTI(data[0].VOIDMTI)

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
                ).show()
            }
        })

        return terminalLiveData
    }

    fun getLiveData(): MutableLiveData<DecryptResponse> {
        return terminalLiveData
    }
}