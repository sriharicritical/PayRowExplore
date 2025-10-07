package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class LoginOTPRequest(@SerializedName("tid") val tid: String)
