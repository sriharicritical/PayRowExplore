package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: OrderData,
    @SerializedName("error") val error: String
)

class OrderData(
    @SerializedName("purchaseBreakdown") val purchaseBreakdown: PurchaseBreakdown,
    @SerializedName("channel") val channel: String,
    @SerializedName("paymentDate") val paymentDate: String,
    @SerializedName("totalTaxAmount") val totalTaxAmount: String,
    @SerializedName("totalAmount") val totalAmount: String,
    @SerializedName("mainMerchantId") val mainMerchantId: String,
    @SerializedName("merchantEmail") val merchantEmail: String,
    @SerializedName("trnNo") val trnNo: String,
    @SerializedName("receiptNo") val receiptNo: String,
    @SerializedName("payrowInvoiceNo") val payrowInvoiceNo: String,
    @SerializedName("balance") val balance: String,
    @SerializedName("cashReceived") val cashReceived: String,
    @SerializedName("checkoutStatus") var checkoutStatus: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("merchantId") val merchantId: String,
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
    @SerializedName("amount") var amount: String,
    @SerializedName("PartialApprovedAmount") val PartialApprovedAmount: String?,
    @SerializedName("merchantBankTransferReturnUrl") var merchantBankTransferReturnUrl: String,
    @SerializedName("cardType") val cardType: String?,
    @SerializedName("cardNumber") val cardNumber: String?,
    @SerializedName("authorizationId") var authorizationId: String?,
    @SerializedName("checkoutUrl") var checkoutUrl: String
)

class OrderDetails(
    @SerializedName("catDetails") val catDetails: CatDetails,
    @SerializedName("itemDetails") val itemDetails: ArrayList<ItemDetails>,
    @SerializedName("_id") val _id: String
)

class CatDetails(
    @SerializedName("catId") val catId: String,
    @SerializedName("categoryName") val categoryName: String
)

class ItemDetails(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("_id") val _id: String
)

class Value(@SerializedName("cashValue") val cashValue: String)
