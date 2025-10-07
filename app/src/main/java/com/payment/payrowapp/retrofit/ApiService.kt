package com.payment.payrowapp.retrofit

import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.dataclass.InitKey
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("onboarding/login/login")
    fun login(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOTPRequestClass: VerifyOTPRequestClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("api/complaints")
    fun postComplaint(
        @HeaderMap headers: Map<String, String>,
        @Body complaintRequest: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("api/complaints/{terminalId}")
    fun getAllComplaints(
        @HeaderMap headers: Map<String, String>,
        @Path("terminalId") terminalId: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @PUT("api/complaints/{id}")
    fun updateComplaintStatus(
        @HeaderMap headers: Map<String, String>,
        @Path("id") complaintId: String,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("api/orders/totalAmount/{userId}")
    fun getTotalAmount(
        @HeaderMap headers: Map<String, String>,
        @Path("userId") userId: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("api/orders/sales")
    fun getTotalSales(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("api/orders/totalReport/{userId}")
    fun getTotalReport(
        @HeaderMap headers: Map<String, String>,
        @Path("userId") userId: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @PUT("api/orders")
    fun addOrder(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @PUT("api/orders/payInvoice")
    fun getPaymentInvoice(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/payrow/purchaseorderlink")
    fun getPaymentLink(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("pos/purchase")
    fun doPurchase(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("invoice/generate/invoice/{InvoiceNumber}")
    fun getPDFReceipt(
        @HeaderMap headers: Map<String, String>,
        @Path("InvoiceNumber") invoiceNumber: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("mobileapis/payrow/getQrCodeOrderDetails/{InvoiceNumber}")
    fun getQRCodeInvoice(
        @HeaderMap headers: Map<String, String>,
        @Path("InvoiceNumber") invoiceNumber: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("mobileapis/posServices/{merchantId}")
    fun getAddItems(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("api/contact/contactCreation")
    fun postContactUsDetails(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @POST("mobileapis/payrow/payrowGenerateQR")
    fun generateQRCode(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/payrow/paymentdetails")
    fun getDailyReport(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/payrow/paymentdetails")
    fun getInvoiceReport(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("onboarding/login/code")
    fun getOTP(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @PUT("onboarding/login/otpVerify")
    fun verifyOTP(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOTPRequestClass: VerifyOTPRequestClass
    ): Call<DecryptResponse>

    @PUT("onboarding/login/verify")
    fun verifyAuthCode(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOTPRequestClass: VerifyOTPRequestClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("onboarding/login/checkDevice")
    fun verifyDevice(
        @HeaderMap headers: Map<String, String>,
        @Body verifyDeviceRequest: EncryptDataClass
    ): Call<DecryptResponse>

    /* @Headers("Content-Type: application/json")
     @POST("onboarding/login/checkDevice")
     fun verifyDevice(@Body verifyDeviceRequest: VerifyDeviceRequest): Call<VerifyDeviceResponse>*/

    @POST("onboarding/login/pin")
    fun createPIN(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOTPRequestClass: VerifyOTPRequestClass
    ): Call<DecryptResponse>

    @GET("onboarding/login/imeiCheck/{IMEI}")
    fun imeiCheck(
        @HeaderMap headers: Map<String, String>,
        @Path("IMEI") IMEI: String
    ): Call<DecryptResponse>

    @GET("onboarding/login/find/{Email}/{MobileNUmber}")
    fun requestTID(
        @HeaderMap headers: Map<String, String>,
        @Path("Email") Email: String,
        @Path("MobileNUmber") MobileNUmber: String
    ): Call<DecryptResponse>

    /* @Headers("Content-Type", "application/x-www-form-urlencoded")
     @Multipart
     @POST
     fun postQRResponse(@Url url: String, @Part("payload") qrCallBackOrderData: QRCallBackOrderData): Call<String>*/

    @FormUrlEncoded
    @POST
    fun postQRResponse(
        @Url url: String,
        @Field("payload") qrCallBackOrderData: String
    ): Call<String>

    @POST("mobileapis/payrow/monthly")
    fun getMonthlyReport(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/payrow/sendUrl")
    fun sendURL(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>


    @Headers("Content-Type: application/json")
    @GET("api/terminaldata/terminaldata")
    fun getTerminalData(
        @HeaderMap headers: Map<String, String>
    ): Call<DecryptResponse>

    @POST("onboarding/login/loginOtp")
    fun getLoginOTP(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @PUT("api/orders")
    fun refOrder(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @POST("mobileapis/payrow/sendTidreport")
    fun sendTIDReport(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>


    @POST("mobileapis/payrow/getvoid")
    fun ecommerceVoid(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>


    @POST("mobileapis/payrow/getrefund")
    fun ecommerceRefund(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("gateway/payrow/taxPostBack/{orderNumber}")
    fun postBackURL(@Path("orderNumber") orderNumber: String): Call<String>


    @Headers("Content-Type: application/json")
    @POST("fss/purchase")
    fun fssFeeFetch( @HeaderMap headers: Map<String, String>,
                     @Body encryptDataClass: EncryptDataClass): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/pos/purchase")
    fun feePrepare(@HeaderMap headers: Map<String, String>,
                    @Body encryptDataClass: EncryptDataClass): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/pos/posrefund")
    fun feeRefund(@HeaderMap headers: Map<String, String>,
                  @Body encryptDataClass: EncryptDataClass): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("merchant/merchant/initdata")
    fun initKey(@Body initKey: InitKey): Call<InitResponse>

    @Headers("Content-Type: application/json")
    @POST("api/orders")
    fun addCashOrder(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/payrow/getTransactionSummary")
    fun getTransSummary(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("mobileapis/filterData/filter/{merchantId}")
    fun getGatewayItems(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("mobileapis/getPosServbyId/{ID}")
    fun getBarCodeItem(
        @HeaderMap headers: Map<String, String>,
        @Path("ID") ID: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/getDefServ")
    fun getPosGateWayItemCount(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("mobileapis/getGWServbyId/{ID}")
    fun getGatewayBarCodeItem(
        @HeaderMap headers: Map<String, String>,
        @Path("ID") ID: String
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("mobileapis/payrow/inquiryDetails")
    fun gateWayEnquiry(@HeaderMap headers: Map<String, String>,
                       @Body encryptDataClass: EncryptDataClass): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @POST("onboarding/login/midOtp")
    fun getMidOtp(
        @HeaderMap headers: Map<String, String>,
        @Body encryptDataClass: EncryptDataClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @PUT("onboarding/login/verifyMidOtp")
    fun verifyMIDOTP(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOTPRequestClass: VerifyOTPRequestClass
    ): Call<DecryptResponse>

    @Headers("Content-Type: application/json")
    @GET("api/complaints/complaintsCount/{terminalId}")
    fun getComplaintsCount( @HeaderMap headers: Map<String, String>,
                            @Path("terminalId") terminalId: String) : Call<DecryptResponse>
}