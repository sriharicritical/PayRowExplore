package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ComplaintStatusResponse(@SerializedName("status") val success: Boolean,@SerializedName("message") val message: String,
                                   @SerializedName("error") val error: String)
