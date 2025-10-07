package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class DeviceLogoutRequest(@SerializedName("tid") val tid: String,@SerializedName("imeiNumber") val imeiNumber: String)
