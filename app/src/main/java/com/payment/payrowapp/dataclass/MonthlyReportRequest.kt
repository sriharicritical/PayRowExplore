package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class MonthlyReportRequest(
    @SerializedName("year") val year: Int,
    @SerializedName("channel") val channel: String,
    @SerializedName("merchantId") val merchantId: String,
    @SerializedName("key") var key: String,
    @SerializedName("tid") val tid: String
)
