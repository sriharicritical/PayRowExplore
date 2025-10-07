package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName


data class DailyReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ArrayList<DailyReport>,
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String,
    @SerializedName("reportPath") val reportPath: String
)

class DailyReport(
    @SerializedName("_id") val _id: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("totalAmount") val totalAmount: String,
    @SerializedName("paymentDate") val paymentDate: String,
    @SerializedName("checkoutStatus") val checkoutStatus: String,
    @SerializedName("checkoutId") val checkoutId: String,
    @SerializedName("urn") val urn: String,
    @SerializedName("totalTaxAmount") val totalTaxAmount: String,
    @SerializedName("customerBillingCountry") val customerBillingCountry: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("mainMerchantId") val mainMerchantId: String,
    @SerializedName("channel") val channel: String,
    @SerializedName("cardNumber") val cardNumber: String,
    @SerializedName("hostReference") val hostReference: String,
    @SerializedName("vasReference") val vasReference: String,
    @SerializedName("balance") val balance: String,
    @SerializedName("cashReceived") val cashReceived: String,
    @SerializedName("timeField") val timeField: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("cardBrand") val cardBrand: String,
    @SerializedName("authorizationId") val authorizationId: String,
    @SerializedName("cardType") val cardType: String,
    @SerializedName("TVR") val TVR: String,
    @SerializedName("AC_INFO") val AC_INFO: String,
    @SerializedName("AC") val AC: String,
    @SerializedName("AID") val AID: String,
    @SerializedName("posEntryMode") val posEntryMode: String,
    @SerializedName("TRANSACTION_TYPE") val TRANSACTION_TYPE: Int,
    @SerializedName("recordType") val recordType: String,
    @SerializedName("responseCode") val responseCode: String,
    @SerializedName("SignatureStatus") val SignatureStatus:Boolean,
    @SerializedName("PinBlockStatus") val PinBlockStatus:Boolean,
    @SerializedName("cardsequencenumber") val cardsequencenumber:String,
    @SerializedName("totamnt") val totamnt:String,
    @SerializedName("PartialApprovedAmount") val PartialApprovedAmount:String?,
    @SerializedName("vatStatus") var vatStatus: Boolean?,
    @SerializedName("vatAmount") var vatAmount: Float?,
    @SerializedName("payRowDigitialFee") val payRowDigitialFee: String?,
    @SerializedName("secondaryCharges") val secondaryCharges: String?,
    @SerializedName("totalServicesAmount") val totalServicesAmount: String?,
    @SerializedName("receiptNo") val receiptNo: String?,
    @SerializedName("auth") val auth: String?,
    @SerializedName("inquiryStatus") val inquiryStatus: Boolean? = null
)
