package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class KeyResponse(
    @SerializedName("result") val result: Result,
    @SerializedName("errors") val errors: Array<String>
)

class Result(@SerializedName("key") val key: String, @SerializedName("pin") val pin: String)
