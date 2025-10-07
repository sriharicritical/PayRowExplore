package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class RefOrderRequest(
    @SerializedName("orderNumber") val orderNumber: String?,
    @SerializedName("checkoutStatus") val checkoutStatus: String?,
    @SerializedName("recordType") val recordType: String?,
    @SerializedName("hostReference") val hostReference: String?,
    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("authorizationId") val authorizationId: String?,
    @SerializedName("orderStatus") val orderStatus: String?,
    @SerializedName("ICCData") val ICCData: String?,
    @SerializedName("paymentDate") val paymentDate: String?,
    @SerializedName("referenceId48") val referenceId48: String?,
    @SerializedName("errorTrackOing") var errorTracking: String?
)
