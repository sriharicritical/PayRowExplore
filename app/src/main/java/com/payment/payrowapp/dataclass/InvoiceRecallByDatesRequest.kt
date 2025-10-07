package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class InvoiceRecallByDatesRequest(
    @SerializedName("dates") val dates: Date, @SerializedName("merchantId") val merchantId: String?,
    @SerializedName("key") var key: String, @SerializedName("tid") val tid: String
)

data class Date(@SerializedName("from") val from: String, @SerializedName("to") val to: String)