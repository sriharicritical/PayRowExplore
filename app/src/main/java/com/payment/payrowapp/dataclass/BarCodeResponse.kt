package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class BarCodeResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: String,
    @SerializedName("status") val status: String
)