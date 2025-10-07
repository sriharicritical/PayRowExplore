package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class EcommerceVoidReqest(
    @SerializedName("paybylinkid") val paybylinkid: String?,
    @SerializedName("checkoutId") val checkoutId: String,
    @SerializedName("gatewayMerchantId") val gatewayMerchantId: String?
)