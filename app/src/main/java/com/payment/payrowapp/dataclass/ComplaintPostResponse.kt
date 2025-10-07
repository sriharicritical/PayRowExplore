package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ComplaintPostResponse(@SerializedName("success") val success:Boolean,
                                 @SerializedName("data") val complaint:Complaints)
