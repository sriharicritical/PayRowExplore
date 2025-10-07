package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class EnquiryRequestClass(@SerializedName("checkoutId") val checkoutId: String,
                               @SerializedName("gatewayMerchantId") val gatewayMerchantId: String)
