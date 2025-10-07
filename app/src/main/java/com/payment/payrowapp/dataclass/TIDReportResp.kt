package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class TIDReportResp(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int
)