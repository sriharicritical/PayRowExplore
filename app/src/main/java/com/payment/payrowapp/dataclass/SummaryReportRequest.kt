package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class SummaryReportRequest(
    @SerializedName("channel") val channel: String?,
    @SerializedName("dates") val dates: SummaryDates,
    @SerializedName("tid") val tid: String,
    @SerializedName("gatewayMid") var gatewayMid: String?,
    @SerializedName("merchantId") val merchantId: String?
)

data class SummaryDates(@SerializedName("to") val to: String,
                        @SerializedName("from") val from: String)