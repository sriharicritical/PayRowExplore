package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class RefundFeeRequest(
    @SerializedName("checkoutId") val checkoutId: String,
    @SerializedName("mainMerchantId") val mainMerchantId: String,
)
