package com.payment.payrowapp.dataclass

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeePrepareResponse(
    @SerializedName("responseCode") val responseCode: Int,
    @SerializedName("data") val data: FeeResponseData,
    @SerializedName("errorMessage") val errorMessage: String
) : Parcelable

@Parcelize
data class FeeResponseData(
    @SerializedName("action") val action: String,
    @SerializedName("version") val version: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("customerAddressLine1") val customerAddressLine1: String,
    @SerializedName("customerAddressLine2") val customerAddressLine2: String,
    @SerializedName("customerEmail") val customerEmail: String,
    @SerializedName("customerPhone") val customerPhone: String,
    @SerializedName("customerAddress") val customerAddress: String,
    @SerializedName("udf10") val udf10: String,
    @SerializedName("langid") val langid: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("servicedata") val servicedata: ArrayList<FeeServiceData>,
    @SerializedName("amount") val amount: String,
    @SerializedName("secondaryCharges") val secondaryCharges: String,
    @SerializedName("refundAmount") val refundAmount: String?
) : Parcelable

@Parcelize
data class FeeServiceData(
    @SerializedName("merchantId") val merchantId: String,
    @SerializedName("serviceId") val serviceId: String,
    @SerializedName("servAmount") val servAmount: String?,
    @SerializedName("noOfTransactions") val noOfTransactions: String,
    @SerializedName("serviceType") val serviceType: String,
) : Parcelable
