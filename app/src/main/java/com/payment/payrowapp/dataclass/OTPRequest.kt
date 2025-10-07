package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class OTPRequest(
    @SerializedName("tid") val tid: String?,
    @SerializedName("merchantId") val merchantId: String?
)
