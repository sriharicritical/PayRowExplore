package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class GenerateQRRequest(
    @SerializedName("paybylinkid") val paybylinkid: String?,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("customerAddressLine1") val customerAddressLine1: String,
    @SerializedName("customerAddressLine2") val customerAddressLine2: String,
    @SerializedName("language") val language: String,
    @SerializedName("channel") val channel: String,
    @SerializedName("governmentServices") val governmentServices: Boolean,
    @SerializedName("addTransactionFeesOnTop") val addTransactionFeesOnTop: Boolean,
    @SerializedName("merchantSiteUrl") val merchantSiteUrl: String,
    @SerializedName("merchantBankTransferReturnUrl") val merchantBankTransferReturnUrl: String,
    @SerializedName("paymentMethodList") val paymentMethodList: Array<String>,
    @SerializedName("sessionTimeoutSecs") val sessionTimeoutSecs: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("urn") val urn: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("customerEmail") val customerEmail: String,
    @SerializedName("customerPhone") val customerPhone: String,
    @SerializedName("customerCity") val customerCity: String,
    @SerializedName("customerState") val customerState: String,
    @SerializedName("customerCountry") val customerCountry: String,
    @SerializedName("customerPostalCode") val customerPostalCode: String,
    @SerializedName("toggleExpiration") val toggleExpiration: Boolean,
    @SerializedName("purchaseDetails") val purchaseDetails: PurchaseDetail,
    @SerializedName("totalServicesAmount") val totalServicesAmount: Float,
    @SerializedName("terminalId") val terminalId: String?,
    @SerializedName("distributorId") val distributorId: String?,
    @SerializedName("terminalEmail") var terminalEmail: String?,
    @SerializedName("terminalPhone") var terminalPhone: String?,
    @SerializedName("businessId") var businessId: String?,
    @SerializedName("checkoutStatus") var checkoutStatus: String?,
    @SerializedName("merchantEmail") val merchantEmail: String?,
    @SerializedName("merchantPhone") val merchantPhone: String?,
    @SerializedName("storeManagerId") val storeManagerId:String?,
    @SerializedName("deviceSerialNumber") val deviceSerialNumber: String?,
    @SerializedName("vatStatus") val vatStatus: Boolean?,
    @SerializedName("vatAmount") val vatAmount: Float?,
    @SerializedName("gatewayMerchantId") val gatewayMerchantId: String?,
    @SerializedName("checkoutId") val checkoutId: String?
)

data class PurchaseDetail(@SerializedName("service") val service: ArrayList<Service>)

