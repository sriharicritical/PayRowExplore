package com.payment.payrowapp.login

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.LoginRequest
import com.payment.payrowapp.dataclass.LoginResponse
import com.payment.payrowapp.dataclass.VerifyOTPRequestClass
import com.payment.payrowapp.introduction.EnterTIDActivity
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object LoginRepository {
    val mutableLiveData = MutableLiveData<DecryptResponse>()

    fun getMutableLiveData(
        context: Context,
        loginRequest: LoginRequest
    ): MutableLiveData<DecryptResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        /* val jsonObject = JSONObject()
         jsonObject.put("tid", loginRequest.tid)
         jsonObject.put("pin", loginRequest.pin)
         jsonObject.put("imeiNumber", loginRequest.imeiNumber)*/

        val jsonString = Gson().toJson(loginRequest)

        val timeStamp = HeaderSignatureUtil.getISO()

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/onboarding/login/login", timeStamp, context)

        // encrypt data
        val encryptData = EncryptDecrypt.encrypt(
            jsonString,
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )

        val verifyOTPRequestClass = VerifyOTPRequestClass(encryptData)
        val jsonString2 = Gson().toJson(verifyOTPRequestClass)

        //generate headers
        val headerMap = ContextUtils.getHeaderMap(
            "",
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(sharedPreferenceUtil,
                "POST",
                "/onboarding/login/login",
                jsonString2
            ),
            context
        )

        ApiClient.apiServiceKYC.login(headerMap, verifyOTPRequestClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>, response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Login Successful!",
                            Toast.LENGTH_LONG
                        ).show()
                        try {

                            val decrypt = EncryptDecrypt.decrypt(
                                response.body()!!.data,
                                secretKey,
                                sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                            )

                            // val jsonObject = JSONObject(decrypt)
                            // val key = jsonObject.getString("data")
                            val gson = Gson()
                            val loginResponse = gson.fromJson(decrypt, LoginResponse::class.java)

                            val jwt = JWT(loginResponse.token.token)
                            Log.d("Login", "Jws role: " + jwt.getClaim("role").asString())
                            Log.d(
                                "Login",
                                "Jws: " + ContextUtils.decoded(loginResponse.token.token)
                            )
                            Log.d("Login", "Token: $jwt")
                            Log.d("Login", "Id: " + jwt.getClaim("id").asString())

                            mutableLiveData.postValue(response.body())

                            sharedPreferenceUtil.setLoginType(jwt.getClaim("role").asString())
                            sharedPreferenceUtil.setID(jwt.getClaim("id").asString())
                            sharedPreferenceUtil.setLoginToken(jwt.toString())
                            sharedPreferenceUtil.setMerchantID(
                                jwt.getClaim("posMerchantId").asString()
                            )
                            sharedPreferenceUtil.setUserID(jwt.getClaim("userId").asString())
                            sharedPreferenceUtil.setRole(jwt.getClaim("role").asString())
                            sharedPreferenceUtil.setStoreID(jwt.getClaim("storeId").asString())
                            sharedPreferenceUtil.setMerchantMobileNumber(
                                jwt.getClaim("mobileNumber").asString()
                            )
                            sharedPreferenceUtil.setEmailID(jwt.getClaim("emailId").asString())
                            sharedPreferenceUtil.setDistributionID(
                                jwt.getClaim("distributorId").asString()
                            )
                            sharedPreferenceUtil.setFirstName(jwt.getClaim("firstName").asString())
                            sharedPreferenceUtil.setLastName(jwt.getClaim("lastName").asString())

                            sharedPreferenceUtil.setBusinessType(
                                jwt.getClaim("businessType").asString()
                            )
                            sharedPreferenceUtil.setBOBox(jwt.getClaim("boBox").asString())
                            sharedPreferenceUtil.setCountry(jwt.getClaim("country").asString())
                            sharedPreferenceUtil.setAddressDetails(
                                jwt.getClaim("addressDetails").asString()
                            )
                            sharedPreferenceUtil.setPayByLinkID(
                                jwt.getClaim("paybylinkid").asString()
                            )
                           // sharedPreferenceUtil.setIV(jwt.getClaim("iv").asString())
                           // sharedPreferenceUtil.setAESKey(jwt.getClaim("key").asString())
                           // sharedPreferenceUtil.setAlgorithm(jwt.getClaim("AES").asString())
                            sharedPreferenceUtil.setURN(jwt.getClaim("urn").asString())

                            sharedPreferenceUtil.setMerchantEmail(
                                jwt.getClaim("merchantEmail").asString()
                            )
                            sharedPreferenceUtil.setMerchantPhone(
                                jwt.getClaim("merchantMobile").asString()
                            )
                            sharedPreferenceUtil.setBussinessId(
                                jwt.getClaim("businessId").asString()
                            )
                            sharedPreferenceUtil.setCity(jwt.getClaim("city").asString())
                            sharedPreferenceUtil.setPKey(jwt.getClaim("publicKey").asString())
                            sharedPreferenceUtil.setZKey(jwt.getClaim("zpkKey").asString())
                            sharedPreferenceUtil.setReportID(jwt.getClaim("reportingID").asString())
                            sharedPreferenceUtil.setSaleID(jwt.getClaim("salesId").asString())
                            sharedPreferenceUtil.setGatewayMerchantID(jwt.getClaim("gatewayMerchantId").asString())

                            sharedPreferenceUtil.setAuthKey(jwt.getClaim("authToken").asString())
                            sharedPreferenceUtil.setHashKey(jwt.getClaim("hashKey").asString())

                            jwt.getClaim("vat").asBoolean()
                                ?.let { sharedPreferenceUtil.setVATCalculator(it) }

                            jwt.getClaim("serviceCatalogue").asBoolean()
                                ?.let { sharedPreferenceUtil.setCataLogAmount(it) }

                            jwt.getClaim("digitalFee").asBoolean()
                                ?.let { sharedPreferenceUtil.setPayRowDigital(it) }

                            jwt.getClaim("barCode").asBoolean()
                                ?.let { sharedPreferenceUtil.setScanBarCode(it) }

                            sharedPreferenceUtil.setLastLogin(System.currentTimeMillis())

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        context.startActivity(Intent(context, DashboardActivity::class.java))
                    } else if (response.code() == 400) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_enter_valid_pin),
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (response.code() == 429) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.tomany_attemts_tryagain),
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (response.code() == 300) {

                        try {
                            sharedPreferenceUtil.clearPreferences()
                        } catch (e: IllegalArgumentException) {

                        }
                        Toast.makeText(
                            context,
                            context.getString(R.string.imei_no_not_verified),
                            Toast.LENGTH_LONG
                        ).show()
                        sharedPreferenceUtil.setISLogin(false)
                        /*  editor.putBoolean(Constants.IS_LOGIN, false)
                          editor.apply()*/
                        context.startActivity(
                            Intent(
                                context,
                                EnterTIDActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
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

    fun getLiveData(): MutableLiveData<DecryptResponse> {
        return mutableLiveData
    }
}