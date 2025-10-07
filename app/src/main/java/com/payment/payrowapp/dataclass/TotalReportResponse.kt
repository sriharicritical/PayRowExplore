package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class TotalReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ArrayList<TotalReport>,
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String
)

class TotalReport(@SerializedName("_id") val _id: TotalReportId)
