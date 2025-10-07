package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class AuthCodeRequest(
    @SerializedName("code") val code: String,
    @SerializedName("tid") val tid: String,
    @SerializedName("imeiNumber") val imeiNumber: String,
    @SerializedName("keyValidation") val keyValidation: String,
    @SerializedName("keys") val keys: Boolean,
    @SerializedName("sunmiStatus") val sunmiStatus: String?
)