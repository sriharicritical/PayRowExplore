package com.payment.payrowapp.retrofit

import android.util.Log.VERBOSE
//import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val BASE_URL_SMS: String = "http://www.ducont.ae/NotisifyRestAPI/api/scheduler/"

    //    private const val BASE_URL: String = "https://payrowdev.uaenorth.cloudapp.azure.com"
    private const val BASE_URL: String = "https://payrowqa.payrow.ae"
    private const val BASE_URL_KYC: String = "https://payrowqa.payrow.ae"
    private const val BASE_URL_GATEWAY: String = "https://gatewaydev.payrow.ae"

    // private const val BASE_URL_GATEWAY: String = "https://payrowdev.uaenorth.cloudapp.azure.com"
   // public const val BASE_URL_SOFT_POS: String = "https://payrowdev.uaenorth.cloudapp.azure.com"

    // productionURL
     const val BASE_URL_SOFT_POS: String = "https://payrow.ae"

    const val MERCHANT_BANK_TRANS_URL =
        "$BASE_URL_SOFT_POS/gateway/payrow/reponseCheck"
    const val GENERATE_INVOICE = "/invoice/generate/invoice/"
    const val SUMMARY_INVOICE = "/invoice/summeryinvoice/"

    val gson: Gson by lazy {
        GsonBuilder().setLenient().create()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL_SOFT_POS)
            .client(getMyHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val retrofitKYC: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL_SOFT_POS)
            .client(getMyHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    private val retrofitSMS: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL_SOFT_POS)
            .client(getMyHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    private val retrofitGateway: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL_SOFT_POS)
            .client(getMyHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    private val retrofitSoftPOS: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL_SOFT_POS)
            .client(getMyHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiServiceSMS: ApiService by lazy {
        retrofitSMS.create(ApiService::class.java)
    }
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    val apiServiceKYC: ApiService by lazy {
        retrofitKYC.create(ApiService::class.java)
    }
    val apiServiceGateway: ApiService by lazy {
        retrofitGateway.create(ApiService::class.java)
    }
    val apiSoftPOS: ApiService by lazy {
        retrofitSoftPOS.create(ApiService::class.java)
    }

    private fun getMyHttpClient(): OkHttpClient.Builder {

        //  val interceptor = HttpLoggingInterceptor()
        //  interceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()

       /* httpClient.addInterceptor(
            LoggingInterceptor.Builder()
                .setLevel(Level.BODY)
                .log(VERBOSE)
                .build())*/

        httpClient.connectTimeout(15, TimeUnit.SECONDS)
        httpClient.connectTimeout(15, TimeUnit.SECONDS)
        httpClient.readTimeout(35, TimeUnit.SECONDS)

        return httpClient
    }
}