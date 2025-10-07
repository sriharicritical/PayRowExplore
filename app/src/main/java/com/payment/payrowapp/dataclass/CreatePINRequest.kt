package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class CreatePINRequest(
    @SerializedName("terminalId") val terminalId: String,
    @SerializedName("pin") val pin: String,
    @SerializedName("status") val status: String
)
