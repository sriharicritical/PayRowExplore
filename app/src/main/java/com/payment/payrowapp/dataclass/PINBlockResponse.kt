package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class PINBlockResponse(@SerializedName("result") val result: Result, @SerializedName("errors") val errors:Array<String>)

