package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class ConfigurationResponse(
    @SerializedName("success") val success: Boolean,
    val data: JSONObject
)
