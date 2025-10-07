package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ReqForTIDResp(
    @SerializedName("success") var success: Boolean,
    @SerializedName("message") var message: String
)
