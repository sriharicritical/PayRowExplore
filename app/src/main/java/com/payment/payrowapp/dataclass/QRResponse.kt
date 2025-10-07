package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class QRResponse(@SerializedName("responseCode") val responseCode: Int,
                      @SerializedName("qrCodeURL")  val qrCodeURL:String,
                      @SerializedName("checkoutID")  val checkoutID: String)
