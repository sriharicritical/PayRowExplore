package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName


data class EnquiryResponseDataClass(
    @SerializedName("status") var status: Int?,
    @SerializedName("data") var data: EnquiryDataClass?)

data class EnquiryDataClass( @SerializedName("totalAmount") var totalAmount: String?,
    @SerializedName("vatStatus") var vatStatus: Boolean?,
    @SerializedName("vatAmount") var vatAmount: Float?,
    @SerializedName("auth") var auth: String?,
    @SerializedName("cardNumber") var cardNumber: String?,
    @SerializedName("cardBrand") val cardBrand: String?,
    @SerializedName("_id") val id: String?,
    @SerializedName("responseCode") val responseCode: Int?,
    @SerializedName("orderStatus") val orderStatus: String?,
    @SerializedName("orderNumber") val orderNumber: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("paymentDate") val paymentDate: String?,
    @SerializedName("purchaseDate") val purchaseDate: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("checkoutId") val checkoutId: String?,
    @SerializedName("checkoutStatus") val checkoutStatus: String?,
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("customMessage") val customMessage: String?,
    @SerializedName("respCodeDesc") val respCodeDesc: String?
)




