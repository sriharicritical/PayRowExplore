package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class PaymentLinkResponse(
    @SerializedName("responseCode") val responseCode: String,
    @SerializedName("errorMessage") val errorMessage: String,
    @SerializedName("checkoutId") val checkoutId: String,
    @SerializedName("checkoutUrl") val checkoutUrl: String
)
