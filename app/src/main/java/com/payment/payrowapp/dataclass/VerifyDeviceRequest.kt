package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class VerifyDeviceRequest( @SerializedName("appType") val appType: String,
    @SerializedName("tid") val tid: String, @SerializedName("mobileNumber") val mobileNumber: Long,
    @SerializedName("sn") val sn: String
)
