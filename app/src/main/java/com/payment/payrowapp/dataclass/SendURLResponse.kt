package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class SendURLResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
