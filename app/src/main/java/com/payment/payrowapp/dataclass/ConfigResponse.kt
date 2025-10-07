package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("data") val data: String,
    @SerializedName("success") val success: Boolean
)
