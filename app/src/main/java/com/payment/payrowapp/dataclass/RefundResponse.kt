package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class RefundResponse(@SerializedName("responseCode") val responseCode: Int,
                          @SerializedName("errorMessage")  val errorMessage:String,
                          @SerializedName("authRespcode")  val authRespcode: Int)
