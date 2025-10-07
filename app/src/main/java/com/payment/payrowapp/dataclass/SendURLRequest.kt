package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class SendURLRequest(
    @SerializedName("subject") val subject: String,
    @SerializedName("email") val email: String,
    @SerializedName("url") val url: String?,
    @SerializedName("type") val type: String?
)

