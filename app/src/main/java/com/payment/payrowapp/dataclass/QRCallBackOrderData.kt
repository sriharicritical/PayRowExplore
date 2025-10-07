package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class QRCallBackOrderData(
    @SerializedName("purchaseBreakdown") val purchaseBreakdown: PurchaseBreakdown,
    @SerializedName("channel") val channel: String,
    @SerializedName("paymentDate") val paymentDate: String,
    @SerializedName("totalTaxAmount") val totalTaxAmount: Float,
    @SerializedName("totalAmount") val totalAmount: Float,
    @SerializedName("mainMerchantId") val mainMerchantId: String,
    @SerializedName("checkoutStatus") var checkoutStatus: String,
    @SerializedName("orderStatus") var orderStatus: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("_id") val _id: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("customerEmail") val customerEmail: String,
    @SerializedName("customerPhone") val customerPhone: String,
    @SerializedName("customerBillingCity") val customerBillingCity: String,
    @SerializedName("customerBillingState") val customerBillingState: String,
    @SerializedName("customerBillingCountry") val customerBillingCountry: String,
    @SerializedName("customerBillingPostalCode") val customerBillingPostalCode: String,
    @SerializedName("customerAddressLine1") val customerAddressLine1: String,
    @SerializedName("customerAddressLine2") val customerAddressLine2: String,
    @SerializedName("checkoutId") var checkoutId: String,
    @SerializedName("amount") var amount: Float,
    @SerializedName("PartialApprovedAmount") val PartialApprovedAmount: String?,
    @SerializedName("merchantBankTransferReturnUrl") var merchantBankTransferReturnUrl: String,
    @SerializedName("cardBrand") val cardBrand: String?,
    @SerializedName("cardNumber") val cardNumber: String?,
    @SerializedName("errorMessage") var errorMessage: String?,
    @SerializedName("auth") var auth: String?,
    @SerializedName("checkoutUrl") var checkoutUrl: String
)
