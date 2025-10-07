package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

class SendTIDReportReq(
    @SerializedName("type") val type: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("dates") val dates: ReportDate,
    @SerializedName("merchantId") val merchantId: String?,
    @SerializedName("tid") val tid: String,
    @SerializedName("email") var email: String?,
    @SerializedName("report") var report: Boolean?,
    @SerializedName("gatewayMid") var gatewayMid: String?
)

data class ReportDate(
    @SerializedName("from") val from: String?,
    @SerializedName("to") val to: String?
)