package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class EcommerceVoidResponse(
    @SerializedName("checkoutStatus") val checkoutStatus: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("orderNumber") val orderNumber: String?
)