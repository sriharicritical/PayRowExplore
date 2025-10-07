package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class SummaryReportResp(@SerializedName("data")val data: SummaryData,
                             @SerializedName("success") val success: Boolean)

data class SummaryData( @SerializedName("saleAmount") val saleAmount: String,
                        @SerializedName("saleCount") val saleCount: String,
                        @SerializedName("refundAmount")  val refundAmount: String,
                        @SerializedName("refundCount")  val refundCount: String,
                        @SerializedName("voidAmount")   val voidAmount: String,
                        @SerializedName("voidCount")   val voidCount: String,
                        @SerializedName("totalAmount")  val totalAmount: String,
                        @SerializedName("totalcount")  val totalcount: String,
                        @SerializedName("servCharges")  val servCharges: String
                        )
