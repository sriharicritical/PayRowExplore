package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QRCodeResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: ArrayList<QRData>,
    @SerializedName("errorMessage") val errorMessage: String
)

class QRData(
    @SerializedName("purchaseBreakdown") var purchaseBreakdown: PurchaseBreakdown,
    @SerializedName("_id") var _id: String,
    @SerializedName("responseCode") var responseCode: String,
    @SerializedName("urn") var urn: String,
    @SerializedName("orderNumber") var orderNumber: String,
    @SerializedName("amount") var amount: String,
    @SerializedName("paymentDate") var paymentDate: String,
    @SerializedName("customerName") var customerName: String,
    @SerializedName("customerEmail") var customerEmail: String,
    @SerializedName("customerPhone") var customerPhone: String,
    @SerializedName("customerBillingCity") var customerBillingCity: String,
    @SerializedName("customerBillingState") var customerBillingState: String,
    @SerializedName("customerBillingCountry") var customerBillingCountry: String,
    @SerializedName("customerBillingPostalCode") var customerBillingPostalCode: String,
    @SerializedName("totalTaxAmount") var totalTaxAmount: String,
    @SerializedName("totalAmount") var totalAmount: String,
    @SerializedName("sourceReference") var sourceReference: String,
    @SerializedName("mainMerchantId") var mainMerchantId: String,
    @SerializedName("merchantBankTransferReturnUrl") var merchantBankTransferReturnUrl: String,
    @SerializedName("merchantSiteUrl") var merchantSiteUrl: String,
    @SerializedName("checkoutId") var checkoutId: String,
    @SerializedName("checkoutStatus") var checkoutStatus: String,
    @SerializedName("checkoutUrl") var checkoutUrl: String,
    @SerializedName("errorMessage") var errorMessage: String,
    @SerializedName("channel") var channel: String,
    @SerializedName("visaNumber") var visaNumber: String,
    @SerializedName("posId") var posId: String,
    @SerializedName("__v") var __v: String,
    @SerializedName("posType") var posType: String,
    @SerializedName("receiptNo") var receiptNo: String,
    @SerializedName("storeId") var storeId: String,
    @SerializedName("trnNo") var trnNo: String,
    @SerializedName("userId") var userId: String,
    @SerializedName("payrowInvoiceNo") var payrowInvoiceNo: String,
    @SerializedName("merchantEmail") var merchantEmail: String,
    @SerializedName("distributorId") var distributorId: String,
    @SerializedName("referenceId1") var refundId: String,
    @SerializedName("referenceId7") var refId7: String,
    @SerializedName("referenceId11") var refId11: String,
    @SerializedName("referenceId12") var refId12: String,
    @SerializedName("referenceId13") var refId13: String,
    @SerializedName("referenceId37") var refId37: String,
    @SerializedName("referenceId32") var refId32: String,
    @SerializedName("referenceId33") var refId33: String,
    @SerializedName("authorizationId") var authorizationId: String,
    @SerializedName("hostReference") var hostReference: String,
    @SerializedName("vasReference") var vasReference: String,
    @SerializedName("balance") var balance: String,
    @SerializedName("cashReceived") var cashReceived: String,
    @SerializedName("base64") var base64: String,
    @SerializedName("purchaseAmount") var purchaseAmount: String,
    @SerializedName("orderStatus") var orderStatus: String,
    @SerializedName("cardNumber") var cardNumber: String,
    @SerializedName("cardBrand") var cardBrand: String,
    @SerializedName("purchaseNumber") var purchaseNumber: String,
    @SerializedName("cardType") var cardType: String,
    @SerializedName("track2Data") val track2Data: String,
    @SerializedName("posEntryMode") val posEntryMode: String,
    @SerializedName("TVR") val TVR: String,
    @SerializedName("AC_INFO") val AC_INFO: String,
    @SerializedName("AC") val AC: String,
    @SerializedName("AID") val AID: String,
    @SerializedName("TRANSACTION_TYPE") val TRANSACTION_TYPE: Int,
    @SerializedName("recordType") val recordType: String,
    @SerializedName("ICCData") val ICCData: String,
    @SerializedName("cardExpiryDate") var cardExpiryDate: String?,
    @SerializedName("cardsequencenumber") var cardsequencenumber: String?,
    @SerializedName("SignatureStatus") var SignatureStatus: Boolean,
    @SerializedName("PinBlockStatus") var PinBlockStatus: Boolean,
    @SerializedName("PartialApprovedAmount") val PartialApprovedAmount: String?,
    @SerializedName("vatStatus") var vatStatus: Boolean?,
    @SerializedName("vatAmount") var vatAmount: Float?,
    @SerializedName("payRowDigitialFee") val payRowDigitialFee: String?,
    @SerializedName("secondaryCharges") val secondaryCharges: String?,
    @SerializedName("referenceId48") val referenceId48: String?,
    @SerializedName("pmtTxnRefCode") val pmtTxnRefCode: String?,
    @SerializedName("totalServicesAmount") val totalServicesAmount: String?,
    @SerializedName("auth") var auth: String,
    @SerializedName("refundStatus") var refundStatus: Boolean,
    @SerializedName("inquiryStatus") val inquiryStatus: Boolean? = null
)

class PurchaseBreakdown(
    @SerializedName("service") var service: ArrayList<QRService>,
    @SerializedName("fee") var fee: ArrayList<Fee>
) : Serializable

class QRService(
    @SerializedName("taxDetails") var taxDetails: ArrayList<TaxDetails>,
    @SerializedName("serviceCode") var serviceCode: String,
    @SerializedName("englishName") var englishName: String,
    @SerializedName("arabicName") var arabicName: String,
    @SerializedName("unitPrice") var unitPrice: Float,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("transactionAmount") var transactionAmount: Float,
    @SerializedName("taxApplicable") var taxApplicable: Boolean,
    @SerializedName("totalTaxAmount") var totalTaxAmount: Float,
    @SerializedName("totalAmount") var totalAmount: Float,
    @SerializedName("_id") var _id: String
) : Serializable

class TaxDetails(
    @SerializedName("taxCode") var taxCode: String,
    @SerializedName("taxRegistrationNumber") var taxRegistrationNumber: String,
    @SerializedName("taxAmount") var taxAmount: String,
    @SerializedName("taxableAmount") var taxableAmount: String,
    @SerializedName("_id") var _id: String
) : Serializable

class Fee(
    @SerializedName("taxDetails") var taxDetails: ArrayList<TaxDetails>,
    @SerializedName("feeCode") var feeCode: String,
    @SerializedName("englishName") var englishName: String,
    @SerializedName("arabicName") var arabicName: String,
    @SerializedName("englishDescription") var englishDescription: String,
    @SerializedName("arabicDescription") var arabicDescription: String,
    @SerializedName("type") var type: String,
    @SerializedName("unitPrice") var unitPrice: String,
    @SerializedName("quantity") var quantity: String,
    @SerializedName("taxApplicable") var taxApplicable: String,
    @SerializedName("feeAmount") var feeAmount: String,
    @SerializedName("feeTaxAmount") var feeTaxAmount: String,
    @SerializedName("isDebit") var isDebit: String,
    @SerializedName("_id") var _id: String
) : Serializable
