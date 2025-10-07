package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("imeiNumber") val imeiNumber: String,
    @SerializedName("pin") val pin: String, @SerializedName("tid") val tid: String,
    @SerializedName("latitude") val latitude: String?, @SerializedName("longitude") val longitude: String?,
    @SerializedName("appVersionCheck") val appVersionCheck: String?
)
