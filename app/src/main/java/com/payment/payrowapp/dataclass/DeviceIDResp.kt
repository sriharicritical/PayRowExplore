package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class DeviceIDResp(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: String
)
