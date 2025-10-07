package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class VerifyOTPRequest(
    @SerializedName("code") val code: String,
    @SerializedName("tid") val tid: String?,
    @SerializedName("mid") val mid: String?,
    @SerializedName("keyValidation") val keyValidation: String?
)
