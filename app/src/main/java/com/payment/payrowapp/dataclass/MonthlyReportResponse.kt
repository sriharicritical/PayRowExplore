package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class MonthlyReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ArrayList<TotalCount>,
    @SerializedName("message") val message: String
)

class TotalCount(
    @SerializedName("_id") val _id: Id,
    @SerializedName("count") val count: Int,
    @SerializedName("credit") val credit: Float,
    @SerializedName("month") val month: String,
    @SerializedName("monthnumber") val monthnumber: String
)
