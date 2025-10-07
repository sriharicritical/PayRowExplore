package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class PaymentInvoiceRequest(
    @SerializedName("fromDate") val fromDate: String,
    @SerializedName("toDate") val toDate: String,
    @SerializedName("selected") val selected: String,
    @SerializedName("invoiceNum") val invoiceNum: String
)