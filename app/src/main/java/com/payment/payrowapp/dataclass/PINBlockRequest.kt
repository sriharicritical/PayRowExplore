package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PINBlockRequest(
    @SerializedName("pin") var pin: String?, @SerializedName("pan") var pan: String
) : Serializable