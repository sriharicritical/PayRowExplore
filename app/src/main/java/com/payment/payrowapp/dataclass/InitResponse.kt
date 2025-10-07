package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class InitResponse(@SerializedName("data") val data: InitData,@SerializedName("success") val success: Boolean)

data class InitData(@SerializedName("encryptedData") val encryptedData: String,
                    @SerializedName("validation") val validation: String,@SerializedName("authTag") val authTag: String
)