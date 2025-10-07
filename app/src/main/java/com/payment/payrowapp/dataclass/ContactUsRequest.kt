package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ContactUsRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("did") val did: String?
)