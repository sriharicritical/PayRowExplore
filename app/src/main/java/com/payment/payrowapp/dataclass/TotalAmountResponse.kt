package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class TotalAmountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: TotalAmount,
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String
)

class TotalAmount(
    @SerializedName("totalCash") val totalCash: String,
    @SerializedName("totalSequence") val totalSequence: String,
    @SerializedName("totaltap") val totaltap: String,
    @SerializedName("avgSequence") val avgSequence: String,
    @SerializedName("avgTap") val avgTap: String,
    @SerializedName("avgCash") val avgCash: String,
    @SerializedName("total") val total: Total
)

class Total(
    @SerializedName("totalCredit") val totalCredit: String,
    @SerializedName("count") val count: String,
    @SerializedName("avgValue") val avgValue: String,
    @SerializedName("avgCount") val avgCount: String
)
