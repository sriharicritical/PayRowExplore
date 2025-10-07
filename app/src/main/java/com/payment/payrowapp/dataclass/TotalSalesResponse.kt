package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class TotalSalesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ArrayList<TotalSales>,
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String
)


class TotalSales(
    @SerializedName("_id") val _id: Id,
    @SerializedName("count") val count: Int,
    @SerializedName("total") val total: Float,
    @SerializedName("month") val month: String
)
