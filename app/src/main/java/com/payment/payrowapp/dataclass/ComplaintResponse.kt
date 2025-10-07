package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ComplaintResponse(@SerializedName("success") val success:Boolean,
                             @SerializedName("data") val complaints: ArrayList<Complaints>,
                             @SerializedName("message") val message: String,
                             @SerializedName("error") val error: String)
