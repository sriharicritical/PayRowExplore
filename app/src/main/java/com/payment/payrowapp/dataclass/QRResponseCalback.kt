package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class QRResponseCalback(
    @SerializedName("payload") val payload: QRCallBackOrderData,
)
