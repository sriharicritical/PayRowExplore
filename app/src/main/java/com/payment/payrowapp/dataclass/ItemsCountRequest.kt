package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ItemsCountRequest(@SerializedName("gatewayMid") val gatewayMid: String,
                             @SerializedName("posMid") val posMid: String)
