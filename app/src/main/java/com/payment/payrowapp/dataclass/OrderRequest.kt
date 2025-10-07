package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList


data class OrderRequest(
    @SerializedName("storeId") val storeId: String?,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("channel") val channel: String,
    @SerializedName("mainMerchantId") val mainMerchantId: String,
    @SerializedName("posId") val posId: String?,
    @SerializedName("posType") val posType: String?,
    @SerializedName("posEmail") val posEmail: String,
    @SerializedName("posMobile") val posMobile: String,
    @SerializedName("distributorId") val distributorId: String?,
    @SerializedName("purchaseBreakdown") val purchaseBreakdown: PurchaseBreakdownDetails?,
    @SerializedName("userId") val userId: String,
    @SerializedName("paymentDate") val paymentDate: String?,
    @SerializedName("totalTaxAmount") val totalTaxAmount: Float?,
    @SerializedName("totalAmount") val totalAmount: Float?,
    @SerializedName("merchantEmail") val merchantEmail: String?,
    @SerializedName("merchantPhone") val merchantPhone: String?,
    @SerializedName("trnNo") val trnNo: String?,
    @SerializedName("receiptNo") val receiptNo: String?,
    @SerializedName("txnInvoice") val txnInvoice: String?,
    @SerializedName("payrowInvoiceNo") val payrowInvoiceNo: String?,
    @SerializedName("balance") val balance: Float?,
    @SerializedName("cashReceived") val cashReceived: Int?,
    @SerializedName("cardNumber") val cardNumber: String?,
    @SerializedName("hostReference") var hostReference: String?,
    @SerializedName("vasReference") val vasReference: String?,
    @SerializedName("checkoutStatus") var checkoutStatus: String,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("customerEmail") val customerEmail: String?,
    @SerializedName("customerPhone") val customerPhone: String?,
    @SerializedName("customerBillingCity") val customerBillingCity: String?,
    @SerializedName("customerBillingState") val customerBillingState: String?,
    @SerializedName("customerBillingCountry") val customerBillingCountry: String?,
    @SerializedName("customerBillingPostalCode") val customerBillingPostalCode: String?,
    @SerializedName("checkoutId") val checkoutId: String?,
    @SerializedName("_id") val _id: String?,
    @SerializedName("referenceId1") val referenceId1: String?,
    @SerializedName("referenceId7") var referenceId7: String?,
    @SerializedName("referenceId11") var referenceId11: String?,
    @SerializedName("referenceId12") var referenceId12: String?,
    @SerializedName("referenceId13") var referenceId13: String?,
    @SerializedName("referenceId37") var referenceId37: String?,
    @SerializedName("referenceId32") val referenceId32: String?,
    @SerializedName("referenceId33") val refundId33: String?,
    @SerializedName("responseCode") var responseCode: String?,
    @SerializedName("recordType") val recordType: String,
    @SerializedName("authorizationId") var authorizationId: String?,
    @SerializedName("base64") val base64: String?,
    @SerializedName("errorTrackOing") var errorTracking: String?,
    @SerializedName("purchaseAmount") var purchaseAmount: String?,
    @SerializedName("terminalId") var terminalId: String?,
    @SerializedName("terminalEmail") var terminalEmail: String?,
    @SerializedName("terminalPhone") var terminalPhone: String?,
    @SerializedName("businessId") var businessId: String?,
    @SerializedName("cardExpiryDate") var cardExpiryDate: String?,
    @SerializedName("purchaseNumber") var purchaseNumber: String?,
    @SerializedName("orderStatus") var orderStatus: String?,
    @SerializedName("amount") var amount: Float?,
    @SerializedName("payRowDigitialFee") var payRowDigitialFee: Float?,
    @SerializedName("cardType") val cardType: String?,
    @SerializedName("TVR") val TVR: String?,
    @SerializedName("AC_INFO") val AC_INFO: String?,
    @SerializedName("AC") val AC: String?,
    @SerializedName("AID") val AID: String?,
    @SerializedName("TRANSACTION_TYPE") val TRANSACTION_TYPE: Int?,
    @SerializedName("track2Data") val track2Data: String?,
    @SerializedName("posEntryMode") val posEntryMode: String?,
    @SerializedName("ICCData") val ICCData: String?,
    @SerializedName("cardsequencenumber") val cardsequencenumber: String?,
    @SerializedName("storeManagerId") val storeManagerId:String?,
    @SerializedName("SignatureStatus") val SignatureStatus:Boolean?,
    @SerializedName("PinBlockStatus") val PinBlockStatus:Boolean?,
    @SerializedName("PartialApprovedAmount") val PartialApprovedAmount: Float?,
    @SerializedName("referenceId127_3") val referenceId127_3: String?,
    @SerializedName("referenceId127_22") val referenceId127_22: String?,
    @SerializedName("referenceId127_25") val referenceId127_25: String?,
    @SerializedName("referenceId127_37") val referenceId127_37: String?,
    @SerializedName("referenceId4") val referenceId4: String?,
    @SerializedName("referenceId41") val referenceId41: String?,
    @SerializedName("referenceId42") val referenceId42: String?,
    @SerializedName("referenceId43") val referenceId43: String?,
    @SerializedName("deviceSerialNumber") val deviceSerialNumber: String?,
    @SerializedName("purchaseDate") val purchaseDate: String?,
    @SerializedName("vatStatus") val vatStatus: Boolean?,
    @SerializedName("vatAmount") val vatAmount: Float?,
    @SerializedName("referenceId48") var referenceId48: String?,
    @SerializedName("pmtTxnRefCode") val pmtTxnRefCode: String?
) : Serializable

data class PurchaseBreakdownDetails(@SerializedName("service") val service: ArrayList<ItemDetail>) :
    Serializable

data class OrderDetail(
    @SerializedName("catDetails") val catDetails: CatDetail,
    @SerializedName("itemDetails") val itemDetails: ArrayList<ItemDetail>,
)

data class CatDetail(
    @SerializedName("catId") val catId: String,
    @SerializedName("categoryName") val categoryName: String
)

data class ItemDetail(
    @SerializedName("serviceCode") val serviceCode: String,
    @SerializedName("serviceCat") val serviceCat: String,
    @SerializedName("englishName") val englishName: String?,
    @SerializedName("arabicName") val arabicName: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("transactionAmount") var transactionAmount: Double,
    @SerializedName("totalAmount") val totalAmount: Double
) : Serializable

data class TapValue(@SerializedName("tapValue") val tapValue: String)
