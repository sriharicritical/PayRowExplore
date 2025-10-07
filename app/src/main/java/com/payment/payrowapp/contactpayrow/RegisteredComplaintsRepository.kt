package com.payment.payrowapp.contactpayrow

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

object RegisteredComplaintsRepository {
    val mutableLiveData = MutableLiveData<ComplaintResponse>()
    val complaintStatusLiveData = MutableLiveData<DecryptResponse>()

    fun getMutableLiveData(context: Context): MutableLiveData<ComplaintResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/api/complaints/" + sharedPreferenceUtil.getTerminalID(),
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "GET",
                "/api/complaints/" + sharedPreferenceUtil.getTerminalID(),
                "{}"
            ),
            context
        )
        ApiClient.apiService.getAllComplaints(headerMap, sharedPreferenceUtil.getTerminalID())
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

                        val complaintResponse = Gson().fromJson(
                            decrypt.toString(),
                            ComplaintResponse::class.java
                        )
                        mutableLiveData.postValue(complaintResponse)
                    } else {

                        Toast.makeText(
                            context,
                            context.getString(R.string.complaints_not_getting),
                            Toast.LENGTH_LONG
                        ).show()
                        //   Log.i("Response","resp->"+ Gson().toJson(response))
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

    fun updateComplaintStatus(
        context: Context,
        complaintId: String,
        complaintStatus: ComplaintStatus
    ): MutableLiveData<DecryptResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(complaintStatus)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "PUT",
                "/api/complaints/$complaintId",
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
                "PUT",
                "/api/complaints/$complaintId",
                jsonString2
            ),
            context
        )
        ApiClient.apiService.updateComplaintStatus(
            headerMap,
            complaintId,
            encryptDataClass
        ).enqueue(object :
            Callback<DecryptResponse> {
            override fun onResponse(
                call: Call<DecryptResponse>,
                response: Response<DecryptResponse>
            ) {
                Log.d("Request", "req->" + call.request().body.toString())
                if (response.isSuccessful && response.code() == 200) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.compalints_status_updeted),
                        Toast.LENGTH_LONG
                    ).show()
                    complaintStatusLiveData.postValue(response.body())
                } else if (response.code() == 404) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.not_found),
                        Toast.LENGTH_LONG
                    ).show()
                    /* Log.i("Response","resp->"+ Gson().toJson(response))*/
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.update_failed),
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

        return complaintStatusLiveData
    }

    fun getLiveData(): MutableLiveData<ComplaintResponse> {
        return mutableLiveData
    }

    fun getComplaintStatusData(): MutableLiveData<DecryptResponse> {
        return complaintStatusLiveData
    }

    fun getComplaintCountMutableLiveData(context: Context): MutableLiveData<ComplaintsCount> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey = HeaderSignatureUtil.SecretKey(
            "GET",
            "/api/complaints/complaintsCount/" + sharedPreferenceUtil.getTerminalID(),
            timeStamp,
            context
        )

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "GET",
                "/api/complaints/complaintsCount/" + sharedPreferenceUtil.getTerminalID(),
                "{}"
            ),
            context
        )
        ApiClient.apiService.getComplaintsCount(headerMap, sharedPreferenceUtil.getTerminalID())
            .enqueue(object :
                Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    Log.d("Request", "req->" + call.request().body.toString())
                    if (response.isSuccessful && response.body()?.data != null) {
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()?.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        if (decrypt != null) {
                            val complaintsCount = Gson().fromJson(
                                decrypt.toString(),
                                ComplaintsCount::class.java
                            )
                            countResponse.postValue(complaintsCount)
                        }
                    } else {

                        /* Toast.makeText(
                             context,
                             context.getString(R.string.complaints_not_getting),
                             Toast.LENGTH_LONG
                         ).show()*/
                        //   Log.i("Response","resp->"+ Gson().toJson(response))
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

        return countResponse
    }

    fun getCountLiveData(): MutableLiveData<ComplaintsCount> {
        return countResponse
    }

    private var countResponse = MutableLiveData<ComplaintsCount>()
}