package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class OTPResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("responseCode") val responseCode: String,
    @SerializedName("responseMessage") val responseMessage: String,
    @SerializedName("encrypt") var encrypt: String,
    @SerializedName("data") val data: String
)
