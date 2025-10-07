package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: Token, @SerializedName("status") val status: String
)

data class Token(@SerializedName("token") val token: String)