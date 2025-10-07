package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class DailyReportRequest(
    @SerializedName("type") val type: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("dates") val dates: Dates,
    @SerializedName("merchantId") val merchantId: String?,
    @SerializedName("key") var key: String,
    @SerializedName("tid") val tid: String
)

data class Dates(@SerializedName("from") val from: String, @SerializedName("to") val to: String)
