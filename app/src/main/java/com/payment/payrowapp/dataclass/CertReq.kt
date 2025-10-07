package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class CertReq(@SerializedName("encryptedData") val encryptedData: String,@SerializedName("key") val key: String?)
