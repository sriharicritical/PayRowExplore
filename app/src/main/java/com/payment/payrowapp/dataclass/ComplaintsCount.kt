package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ComplaintsCount(@SerializedName("success") val success:Boolean,
                           @SerializedName("data") val data: DataCount,
                           @SerializedName("message") val message: String,
                           @SerializedName("status") val status: Int)

data class DataCount(@SerializedName("count") val count: Int)
