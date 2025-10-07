package com.payment.payrowapp.login

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.EncryptDecrypt
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.refundandreversal.RefundConfirmationActivity
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.retrofit.ApiKeys
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.sunmipay.EmvUtil
import com.payment.payrowapp.sunmipay.ThreadPoolUtil
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets

object AuthenticationRepository {

    val mutableLiveData = MutableLiveData<DecryptResponse>()
    private val verifyOTPMutableLiveData = MutableLiveData<DecryptResponse>()
    private val verifyAuthCodeMutableLiveData = MutableLiveData<DecryptResponse>()
    private val loginMutableLiveData = MutableLiveData<DecryptResponse>()
    fun getMutableLiveData(
        context: Context,
        otpRequest: OTPRequest
    ): MutableLiveData<DecryptResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val jsonString = Gson().toJson(otpRequest)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/onboarding/login/code", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonString,
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )
        val encryptDataClass = EncryptDataClass(encryptData)

        val jSONObject = JSONObject()
        jSONObject.put("data", encryptData)
        //  val jsonString2 = Gson().toJson(encryptDataClass)

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/onboarding/login/code",
                jSONObject.toString()
            ),
            context
        )
        ApiClient.apiServiceKYC.getOTP(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.otp_sent),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            context,
                            response.message().toString(),
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

        return mutableLiveData
    }

    fun verifyOTPMutableLiveData(
        context: Context,
        verifyOTPRequest: VerifyOTPRequest
    ): MutableLiveData<DecryptResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val jsonObject = JSONObject()
        jsonObject.put("code", verifyOTPRequest.code)
        jsonObject.put("tid", verifyOTPRequest.tid)
        jsonObject.put("keyValidation", verifyOTPRequest.keyValidation)

        /* val encrypt = EncryptDecrypt.encrypt(
             jsonObject.toString(),
             sharedPreferenceUtil.getAESKey(),
             sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlg()
         )*/

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("PUT", "/onboarding/login/otpVerify", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonObject.toString(),
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )

        if (!encryptData.isNullOrEmpty()) {
            val verifyOTPRequestClass = VerifyOTPRequestClass(encryptData)

            val jsonString2 = Gson().toJson(verifyOTPRequestClass)

            val headerMap = ContextUtils.getHeaderMap(
                "Bearer " + sharedPreferenceUtil.getAuthToken(),
                timeStamp,
                HeaderSignatureUtil.CreateHeaderSignature(
                    sharedPreferenceUtil,
                    "PUT",
                    "/onboarding/login/otpVerify",
                    jsonString2
                ),
                context
            )

            ApiClient.apiServiceKYC.verifyOTP(
                headerMap,
                verifyOTPRequestClass
            ).enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.body() != null && !response.body()!!.data.isNullOrEmpty()) {
                        /*  val decrpt = EncryptDecrypt.decrypt(
                              response.body()!!.encrypt,
                              sharedPreferenceUtil.getAESKey(),
                              sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlgorithm()
                          )*/
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val jsonObject = JSONObject(decrypt)
                        val key = jsonObject.getString("data")
                        val status = jsonObject.getString("success")
                        val message = jsonObject.getString("message")
                        if (status.equals("true")) {
                            if (key == verifyOTPRequest.keyValidation) {
                                verifyOTPMutableLiveData.postValue(response.body())
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.otp_verified),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.otp_invalid),
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
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.otp_invalid),
                Toast.LENGTH_LONG
            ).show()
        }
        return verifyOTPMutableLiveData
    }

    fun verifyAuthCodeMutableLiveData(
        context: Context,
        authCodeRequest: AuthCodeRequest, sharedPreferenceUtil: SharedPreferenceUtil
    ): MutableLiveData<DecryptResponse> {

        val BDKID = (100000..999999).shuffled().last()
        val KSNValue = "FFFF$BDKID" + ContextUtils.randomValue()
            .toString() + ContextUtils.randomValue().toString()

        //Interface Device Serial Number
        /* val deviceSN = HeaderSignatureUtil.getDeviceSN()
         val deviceSNLength = deviceSN.length
         val ksnValue = "FFFF" + deviceSN.substring(
             deviceSNLength - 11,
             deviceSNLength
         ) + ContextUtils.randomValue().toString()*/

        val jsonObject = JSONObject()
        jsonObject.put("code", authCodeRequest.code)
        jsonObject.put("tid", authCodeRequest.tid)
        jsonObject.put("keyValidation", authCodeRequest.keyValidation)
        jsonObject.put("imeiNumber", authCodeRequest.imeiNumber)
        jsonObject.put("keys", authCodeRequest.keys)
        jsonObject.put("sunmiStatus", authCodeRequest.sunmiStatus)

        if (authCodeRequest.keys) {
            jsonObject.put(
                "keyName",
                "SERIALN" + ContextUtils.randomValue().toString()
            )//deviceSN.substring(deviceSNLength - 4, deviceSNLength))
            jsonObject.put("sn", HeaderSignatureUtil.getDeviceSN())
            jsonObject.put("ksn", KSNValue)
        }

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("PUT", "/onboarding/login/verify", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonObject.toString(),
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )


        if (!encryptData.isNullOrEmpty()) {
            val verifyOTPRequestClass = VerifyOTPRequestClass(encryptData)
            val jsonString2 = Gson().toJson(verifyOTPRequestClass)

            val headerMap = ContextUtils.getHeaderMap(
                "",
                timeStamp,
                HeaderSignatureUtil.CreateHeaderSignature(
                    sharedPreferenceUtil,
                    "PUT",
                    "/onboarding/login/verify",
                    jsonString2
                ),
                context
            )

            ApiClient.apiServiceKYC.verifyAuthCode(headerMap, verifyOTPRequestClass)
                .enqueue(object : Callback<DecryptResponse> {
                    override fun onResponse(
                        call: Call<DecryptResponse>,
                        response: Response<DecryptResponse>
                    ) {
                        if (response.body() != null && response.body()!!.data.isNotEmpty()) {
                            val decrypt = EncryptDecrypt.decrypt(
                                response.body()!!.data,
                                secretKey,
                                sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                            )

                            Log.v("decryptData", decrypt.toString())
                            val jsonObject = JSONObject(decrypt)
                            val key = jsonObject.getString("data")
                            val status = jsonObject.getString("success")
                            val message = jsonObject.getString("message")

                            if (status.equals("true")) {
                                if (key == authCodeRequest.keyValidation) {

                                    verifyAuthCodeMutableLiveData.postValue(response.body())
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.authentication_code_verified),
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val keysObject = jsonObject.getJSONObject("keys")
                                    val merchantId = jsonObject.getString("merchantId")
                                    val terminalId = jsonObject.getString("terminalId")
                                    if (keysObject.toString() != "{}") {
                                        val ectTlVal = keysObject.getString("ectTlVal")
                                        val capability = keysObject.getString("capability")
                                        val TTQ = keysObject.getString("TTQ")
                                        val currencyCode = keysObject.getString("currencyCode")
                                        val addCapability = keysObject.getString("addCapability")
                                        val chipCapability = keysObject.getString("chipCapability")
                                        val terminalType = keysObject.getString("terminalType")
                                        val tapCapability = keysObject.getString("tapCapability")
                                        val currencyExponent =
                                            keysObject.getString("currencyExponent")
                                        val kbpk = keysObject.getString("kbpk")


                                        //save some keys
                                        val sharedPreferenceUtil = SharedPreferenceUtil(context)
                                        sharedPreferenceUtil.setTTQ(TTQ)
                                        sharedPreferenceUtil.setChipCapability(chipCapability)
                                        sharedPreferenceUtil.setTapCapability(tapCapability)
                                        sharedPreferenceUtil.setCurrencyExponent(currencyExponent)
                                        sharedPreferenceUtil.setKBPK(kbpk)


                                        val aidKeysArray = keysObject.getJSONObject("aidkeys")
                                        val aidCapListDataClass = Gson().fromJson(
                                            aidKeysArray.toString(),
                                            AidCapListDataClass::class.java
                                        )

                                        ThreadPoolUtil.executeInCachePool {
                                            ThreadPoolUtil.executeInCachePool {
                                                EmvUtil.injectAIDAndCAPKeys(
                                                    terminalType, context,
                                                    addCapability,
                                                    terminalId,
                                                    merchantId,
                                                    ectTlVal,
                                                    capability,
                                                    TTQ,
                                                    currencyCode,
                                                    aidCapListDataClass
                                                )
                                            }
                                        }
                                        Log.v("AidCAPKeys", aidCapListDataClass.toString())
                                    }

                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.authcode_invalid),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.failure) +
                                    context.getString(R.string.service_unavialable),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                })
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.authcode_invalid),
                Toast.LENGTH_LONG
            ).show()
        }
        return verifyAuthCodeMutableLiveData
    }


    fun getLoginMutableLiveData(
        context: Context,
        loginOTPRequest: LoginOTPRequest, sharedPreferenceUtil: SharedPreferenceUtil
    ): MutableLiveData<DecryptResponse> {
        val jsonString = Gson().toJson(loginOTPRequest)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/onboarding/login/loginOtp", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonString,
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )
        val encryptDataClass = EncryptDataClass(encryptData)

        val jsonString2 = Gson().toJson(encryptDataClass)

        val headerMap = ContextUtils.getHeaderMap(
            "",
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/onboarding/login/loginOtp",
                jsonString2
            ),
            context
        )
        ApiClient.apiServiceKYC.getLoginOTP(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        // val sharedPreferenceUtil = SharedPreferenceUtil(context)

                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )

                        val jsonObject1 = JSONObject(decrypt)

                        val key = jsonObject1.getString("data")

                        val decodedString: ByteArray =
                            Base64.decode(key, Base64.DEFAULT)
                        val text = String(decodedString, StandardCharsets.UTF_8)

                        val jsonObject = JSONObject(text)
                        // sharedPreferenceUtil.setAESKey(jsonObject.getString("key"))
                        // sharedPreferenceUtil.setIV(jsonObject.getString("iv"))
                        //  sharedPreferenceUtil.setAlgorithm(jsonObject.getString("AES"))
                        // sharedPreferenceUtil.setAlg(jsonObject.getString("ALG"))
                        Toast.makeText(
                            context,
                            context.getString(R.string.otp_sent),
                            Toast.LENGTH_LONG
                        ).show()


                    } else {
                        Toast.makeText(
                            context,
                            response.message().toString(),
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

        return loginMutableLiveData
    }

    fun getLiveData(): MutableLiveData<DecryptResponse> {
        return mutableLiveData
    }

    fun getVerifyOTPLiveData(): MutableLiveData<DecryptResponse> {
        return verifyOTPMutableLiveData
    }

    fun getVerifyAuthLiveData(): MutableLiveData<DecryptResponse> {
        return verifyAuthCodeMutableLiveData
    }

    fun sendPurchaseRequestAPI(
        reversal: Boolean,
        cardNumber: String,
        vasRefNo: String,
        mode: String,
        orderNumber: String?,
        context: Context, purchaseRequest: PurchaseRequest
    ): MutableLiveData<PurchaseResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val jsonString = Gson().toJson(purchaseRequest)

        Log.v("void request", jsonString)
        /* val encrypt = EncryptDecrypt.encrypt(
             jsonString!!,
             sharedPreferenceUtil.getAESKey(),
             sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlg()
         )

         val certReq = CertReq(
             encrypt,
             RSAKeyPairGenerator.encryptData(context, sharedPreferenceUtil.getAESKey())
         )*/
        val timeStamp = HeaderSignatureUtil.getISO()
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/pos/purchase", timeStamp, context)

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
                "/pos/purchase",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.doPurchase(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
//                        Toast.makeText(
//                            context,
//                            "Card encrypted data received successfully!",
//                            Toast.LENGTH_LONG
//                        ).show()
                        try {
                            val decrypt = EncryptDecrypt.decrypt(
                                response.body()!!.data,
                                secretKey,
                                sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                            )

                            Log.v("decrypt", decrypt)
                            val purchaseResponse = Gson().fromJson(
                                decrypt.toString(),
                                PurchaseResponse::class.java
                            )
                            /*  if (reversal && purchaseResponse.purchaseResult.responseCode == "00") {
                                  Toast.makeText(
                                      context,
                                      context.getString(R.string.amount_reversal),
                                      Toast.LENGTH_LONG
                                  ).show()
                                  val refOrderRequest = RefOrderRequest(
                                      orderNumber,
                                      "REVERSAL",
                                      "Purchase",
                                      purchaseResponse.purchaseResult.orderNo,
                                      purchaseResponse.purchaseResult.responseCode,
                                      purchaseResponse.purchaseResult.authorizationId,
                                      "REVERSAL",
                                      null, null
                                  )
                                  RefundRepository.getOrderMutableLiveData(
                                      context,
                                      refOrderRequest
                                  )
                                  ReversalDialog(context).show()
                              }*/
                            purchaseResponseResult.postValue(purchaseResponse)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {

                        //   if (!reversal) {
                        val intent = Intent(context, RefundConfirmationActivity::class.java)
                        intent.putExtra("status", "Cancelled")
                        intent.putExtra("vasRefNO", vasRefNo)
                        intent.putExtra("CARDNO", cardNumber)
                        intent.putExtra("orderNumber", orderNumber)
                        intent.putExtra("mode", mode)
                        context.startActivity(intent)
                        Toast.makeText(
                            context,
                            context.getString(R.string.error) + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        //   }
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
                    // if (!reversal) {
                    val intent = Intent(context, RefundConfirmationActivity::class.java)
                    intent.putExtra("status", "Cancelled")
                    intent.putExtra("vasRefNO", vasRefNo)
                    intent.putExtra("CARDNO", cardNumber)
                    intent.putExtra("orderNumber", orderNumber)
                    intent.putExtra("mode", mode)
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        context.getString(R.string.failure) + context.getString(R.string.service_unavialable),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    // }
                }
            })

        return purchaseResponseResult
    }

    fun getPurchaseResponse(): MutableLiveData<PurchaseResponse> {
        return purchaseResponseResult
    }

    val purchaseResponseResult = MutableLiveData<PurchaseResponse>()

    val ecommVoidResponse = MutableLiveData<EcommerceVoidResponse>()

    fun setEcommerceVoid(
        mode: String,
        orderNumber: String,
        context: Context
    ): MutableLiveData<EcommerceVoidResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val ecommerceVoidReqest = EcommerceVoidReqest(
            null,
            orderNumber, sharedPreferenceUtil.getGatewayMerchantID()
        )
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(ecommerceVoidReqest)

        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/getvoid",
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
                "/mobileapis/payrow/getvoid",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.ecommerceVoid(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
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

                            Log.v("decrypt", decrypt)

                            val jsonObject = JSONObject(decrypt)
                            if (jsonObject.has("orderDetails")) {
                                val orderDetailsJson = jsonObject.getJSONObject("orderDetails")
                                val checkoutStatus = orderDetailsJson.getString("checkoutStatus")
                                val amount = orderDetailsJson.getString("amount")
                                val ecommerceVoidResponse =
                                    EcommerceVoidResponse(checkoutStatus, amount, "")
                                /* val ecommerceVoidResponse = Gson().fromJson(
                                     decrypt.toString(),
                                     EcommerceVoidResponse::class.java
                                 )*/
                                ecommVoidResponse.postValue(ecommerceVoidResponse)
                            } else {
                                if (jsonObject.has("errorMessage")) {
                                    Toast.makeText(
                                        context,
                                        jsonObject.getString("errorMessage"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                val ecommerceVoidResponse = Gson().fromJson(
                                    decrypt.toString(),
                                    EcommerceVoidResponse::class.java
                                )
                                ecommVoidResponse.postValue(ecommerceVoidResponse)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

        return ecommVoidResponse
    }

    fun getEcommerceVoid(): MutableLiveData<EcommerceVoidResponse> {
        return ecommVoidResponse
    }

    fun setEcommerceRefund(
        mode: String,
        orderNumber: String,
        context: Context
    ): MutableLiveData<EcommerceVoidResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val ecommerceVoidReqest = EcommerceVoidReqest(
            null,
            orderNumber, sharedPreferenceUtil.getGatewayMerchantID()
        )
        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(ecommerceVoidReqest)

        Log.v("decrypt", "req" + jsonString)
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/payrow/getrefund",
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
                "/mobileapis/payrow/getrefund",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.ecommerceRefund(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
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

                            Log.v("decrypt", decrypt)
                            val jsonObject = JSONObject(decrypt)
                            if (jsonObject.has("paymentDetailsRefund")) {
                                val orderDetailsJson =
                                    jsonObject.getJSONObject("paymentDetailsRefund")
                                val checkoutStatus = orderDetailsJson.getString("checkoutStatus")
                                val amount = orderDetailsJson.getString("amount")
                                val ecommerceVoidResponse =
                                    EcommerceVoidResponse(
                                        checkoutStatus,
                                        amount,
                                        orderDetailsJson.getString("orderNumber")
                                    )
                                /*val ecommerceVoidResponse = Gson().fromJson(
                                decrypt.toString(),
                                EcommerceVoidResponse::class.java
                            )*/
                                ecommVoidResponse.postValue(ecommerceVoidResponse)
                            } else {
                                if (jsonObject.has("errorMessage")) {
                                    Toast.makeText(
                                        context,
                                        jsonObject.getString("errorMessage"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                val ecommerceVoidResponse = Gson().fromJson(
                                    decrypt.toString(),
                                    EcommerceVoidResponse::class.java
                                )
                                ecommVoidResponse.postValue(ecommerceVoidResponse)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

        return ecommVoidResponse
    }

    fun getEcommerceRefund(): MutableLiveData<EcommerceVoidResponse> {
        return ecommVoidResponse
    }

    fun getFSSFeeAPI(
        context: Context, purchaseRequest: PurchaseRequest, status: Boolean
    ): MutableLiveData<PurchaseResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)
        val jsonString = Gson().toJson(purchaseRequest)

        Log.v("void request", jsonString)

        val timeStamp = HeaderSignatureUtil.getISO()
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/fss/purchase", timeStamp, context)

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
                "/fss/purchase",
                jsonString2
            ),
            context
        )
        ApiClient.apiSoftPOS.fssFeeFetch(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        if (status) {
                            val decrypt = EncryptDecrypt.decrypt(
                                response.body()!!.data,
                                secretKey,
                                sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                            )

                            Log.v("decrypt", decrypt)
                            val purchaseResponse = Gson().fromJson(
                                decrypt.toString(),
                                PurchaseResponse::class.java
                            )
                            purchaseFeeResponseResult.postValue(purchaseResponse)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Error: onResponse" + response.body()?.toString(),
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

        return purchaseFeeResponseResult
    }

    fun getFSSFeeResponse(): MutableLiveData<PurchaseResponse> {
        return purchaseFeeResponseResult
    }

    private val purchaseFeeResponseResult = MutableLiveData<PurchaseResponse>()

    private val feeRefundResponseResult = MutableLiveData<FeePrepareResponse>()

    fun getFeeRefundAPI(
        context: Context, refundFeeRequest: RefundFeeRequest
    ): MutableLiveData<FeePrepareResponse> {
        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val timeStamp = HeaderSignatureUtil.getISO()

        val jsonString = Gson().toJson(refundFeeRequest)

        Log.v("decrypt", "req" + jsonString)
        //generate secret key
        val secretKey =
            HeaderSignatureUtil.SecretKey(
                "POST",
                "/mobileapis/pos/posrefund",
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
                "/mobileapis/pos/posrefund",
                jsonString2
            ),
            context
        )

        ApiClient.apiSoftPOS.feeRefund(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {

                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()!!.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val feePrepareResponse = Gson().fromJson(
                            decrypt.toString(),
                            FeePrepareResponse::class.java
                        )
                        feeRefundResponseResult.postValue(feePrepareResponse)
                    } else {
                        Toast.makeText(
                            context,
                            "Error: onResponse" + response.body()?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {

                }
            })

        return feeRefundResponseResult
    }

    fun getFeeRefundResponse(): MutableLiveData<FeePrepareResponse> {
        return feeRefundResponseResult
    }

    fun getMIDMutableLiveData(
        context: Context,
        otpRequest: OTPRequest
    ): MutableLiveData<DecryptResponse> {

        val sharedPreferenceUtil = SharedPreferenceUtil(context)

        val jsonString = Gson().toJson(otpRequest)

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("POST", "/onboarding/login/midOtp", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonString,
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )
        val encryptDataClass = EncryptDataClass(encryptData)

        val jSONObject = JSONObject()
        jSONObject.put("data", encryptData)
        //  val jsonString2 = Gson().toJson(encryptDataClass)

        val headerMap = ContextUtils.getHeaderMap(
            "Bearer " + sharedPreferenceUtil.getAuthToken(),
            timeStamp,
            HeaderSignatureUtil.CreateHeaderSignature(
                sharedPreferenceUtil,
                "POST",
                "/onboarding/login/midOtp",
                jSONObject.toString()
            ),
            context
        )
        ApiClient.apiServiceKYC.getMidOtp(headerMap, encryptDataClass)
            .enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.code() == 200 && response.isSuccessful) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.otp_sent),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            context,
                            response.message().toString(),
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

        return mutableMIDLiveData
    }

    fun getMIDLiveData(): MutableLiveData<DecryptResponse> {
        return mutableMIDLiveData
    }

    private val mutableMIDLiveData = MutableLiveData<DecryptResponse>()


    fun verifyMIDOTPMutableLiveData(service: String,status: Boolean,
                                    context: Context,
                                    verifyOTPRequest: VerifyOTPRequest
    ): MutableLiveData<DecryptResponse> {
        val sharedPreferenceUtil =  SharedPreferenceUtil(context)
        val jsonObject = JSONObject()
        jsonObject.put("code", verifyOTPRequest.code)
        jsonObject.put("merchantId", verifyOTPRequest.mid)
        jsonObject.put("keyValidation", verifyOTPRequest.keyValidation)
        jsonObject.put(service,status)

        /* val encrypt = EncryptDecrypt.encrypt(
             jsonObject.toString(),
             sharedPreferenceUtil.getAESKey(),
             sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlg()
         )*/

        val timeStamp = HeaderSignatureUtil.getISO()
        val secretKey =
            HeaderSignatureUtil.SecretKey("PUT", "/onboarding/login/verifyMidOtp", timeStamp, context)

        val encryptData = EncryptDecrypt.encrypt(
            jsonObject.toString(),
            secretKey,
            sharedPreferenceUtil.getIV(),
            ApiKeys.AES_Algorithm
        )

        if (!encryptData.isNullOrEmpty()) {
            val verifyOTPRequestClass = VerifyOTPRequestClass(encryptData)

            val jsonString2 = Gson().toJson(verifyOTPRequestClass)

            val headerMap = ContextUtils.getHeaderMap(
                "Bearer " + sharedPreferenceUtil.getAuthToken(),
                timeStamp,
                HeaderSignatureUtil.CreateHeaderSignature(
                    sharedPreferenceUtil,
                    "PUT",
                    "/onboarding/login/verifyMidOtp",
                    jsonString2
                ),
                context
            )

            ApiClient.apiServiceKYC.verifyMIDOTP(
                headerMap,
                verifyOTPRequestClass
            ).enqueue(object : Callback<DecryptResponse> {
                override fun onResponse(
                    call: Call<DecryptResponse>,
                    response: Response<DecryptResponse>
                ) {
                    if (response.body() != null && response.body()?.data!=null) {
                        /*  val decrpt = EncryptDecrypt.decrypt(
                              response.body()!!.encrypt,
                              sharedPreferenceUtil.getAESKey(),
                              sharedPreferenceUtil.getIV(), sharedPreferenceUtil.getAlgorithm()
                          )*/
                        val decrypt = EncryptDecrypt.decrypt(
                            response.body()?.data,
                            secretKey,
                            sharedPreferenceUtil.getIV(), ApiKeys.AES_Algorithm
                        )
                        val jsonObject = JSONObject(decrypt)
                        val key = jsonObject.getString("data")
                        val status = jsonObject.getString("success")
                        val message = jsonObject.getString("message")
                        if (status.equals("true")) {
                            if (key == verifyOTPRequest.keyValidation) {
                                verifyMIDOTPMutableLiveData.postValue(response.body())
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.otp_verified),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.otp_invalid),
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
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.otp_invalid),
                Toast.LENGTH_LONG
            ).show()
        }
        return verifyMIDOTPMutableLiveData
    }

    private val verifyMIDOTPMutableLiveData = MutableLiveData<DecryptResponse>()

    fun getMIDVerifyLiveData(): MutableLiveData<DecryptResponse> {
        return verifyMIDOTPMutableLiveData
    }
}